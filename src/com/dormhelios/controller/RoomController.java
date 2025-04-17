package com.dormhelios.controller;

import com.dormhelios.model.dao.RoomDAO;
import com.dormhelios.model.entity.Room;
import com.dormhelios.view.MainDashboardView; // Needed for parenting dialogs
import com.dormhelios.view.RoomFormDialog; // The Add/Edit dialog
import com.dormhelios.view.RoomListView; // The panel this controller manages

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for managing the RoomListView and interacting with RoomDAO.
 * Handles displaying rooms, adding, editing, deleting, searching, and
 * filtering.
 */
public class RoomController {

    private static final Logger LOGGER = Logger.getLogger(RoomController.class.getName());

    private final RoomListView roomListView;
    private final RoomDAO roomDAO;
    private final MainDashboardView mainView; // Parent frame for dialogs

    private RoomFormDialog roomFormDialog; // Instance of the Add/Edit dialog

    public RoomController(RoomListView roomListView, RoomDAO roomDAO, MainDashboardView mainView) {
        this.roomListView = roomListView;
        this.roomDAO = roomDAO;
        this.mainView = mainView; // Store parent frame reference

        attachListeners();
    }

    /**
     * Attaches listeners to the components within RoomListView.
     */
    private void attachListeners() {
        roomListView.addAddRoomButtonListener(e -> openAddRoomDialog());
        roomListView.addEditRoomButtonListener(e -> openEditRoomDialog());
        roomListView.addDeleteButtonListener(e -> deleteRoom());
        // Add listener for View button if implemented separately
        // roomListView.addViewButtonListener(e -> viewRoomDetails());

        // Listener for live search
        roomListView.addSearchFieldListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                roomListView.filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                roomListView.filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                roomListView.filterTable();
            }
        });

        // Listener for filter changes
        roomListView.addFilterComboBoxListener(e -> roomListView.filterTable());
    }

    /**
     * Loads room data asynchronously and updates the view. Typically called
     * when the RoomListView panel is displayed.
     */
    public void loadInitialData() {
        // Use SwingWorker to fetch data off the EDT
        SwingWorker<List<Room>, Void> worker = new SwingWorker<List<Room>, Void>() {
            @Override
            protected List<Room> doInBackground() throws Exception {
                LOGGER.info("Loading room data in background...");
                return roomDAO.findAll(); // Fetch all rooms
            }

            @Override
            protected void done() {
                try {
                    List<Room> rooms = get(); // Get results
                    // Update UI on the EDT
                    roomListView.displayRooms(rooms);
                    updateSummaryCards(rooms); // Update counts based on fetched data
                    LOGGER.info("Room data loaded and view updated.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING, "Room data loading interrupted", e);
                } catch (ExecutionException e) {
                    LOGGER.log(Level.SEVERE, "Error loading room data", e.getCause());
                    roomListView.displayErrorMessage("Error loading room data: " + e.getCause().getMessage());
                }
            }
        };
        worker.execute(); // Start the worker
    }

    /**
     * Updates the summary cards based on the provided list of rooms.
     *
     * @param rooms The list of all rooms.
     */
    private void updateSummaryCards(List<Room> rooms) {
        if (rooms == null) {
            return;
        }
        int total = rooms.size();
        int vacant = (int) rooms.stream().filter(r -> r.getStatus() == Room.RoomStatus.VACANT).count();
        int occupied = (int) rooms.stream().filter(r -> r.getStatus() == Room.RoomStatus.OCCUPIED).count();
        int maintenance = (int) rooms.stream().filter(r -> r.getStatus() == Room.RoomStatus.UNDER_MAINTENANCE).count();
        roomListView.updateSummaryCards(total, vacant, occupied, maintenance);
    }

    /**
     * Opens the RoomFormDialog for adding a new room.
     */
    private void openAddRoomDialog() {
        LOGGER.info("Opening Add Room dialog.");
        // Create dialog if it doesn't exist or reuse if appropriate
        if (roomFormDialog == null) {
            roomFormDialog = new RoomFormDialog(mainView, true); // Pass parent frame
            // Add listener for the save button within the dialog
            roomFormDialog.addSaveButtonListener(e -> saveNewRoom());
            roomFormDialog.addCancelButtonListener(e -> roomFormDialog.closeDialog());
        }
        roomFormDialog.setupForAdd(); // Configure dialog for adding
        roomFormDialog.showDialog(); // Display the modal dialog

        // After dialog closes, check if saved and refresh list
        if (roomFormDialog.isSaved()) {
            loadInitialData(); // Reload data to show the new room
        }
    }

    /**
     * Opens the RoomFormDialog for editing the selected room.
     */
    private void openEditRoomDialog() {
        int selectedRoomId = roomListView.getSelectedRoomId();
        if (selectedRoomId < 0) {
            roomListView.displayErrorMessage("Please select a room to edit.");
            return;
        }
        LOGGER.info("Opening Edit Room dialog for ID: " + selectedRoomId);

        // Fetch the full Room object (could use SwingWorker if slow)
        Optional<Room> roomOpt = roomDAO.findById(selectedRoomId);

        if (roomOpt.isPresent()) {
            if (roomFormDialog == null) {
                roomFormDialog = new RoomFormDialog(mainView, true);
                roomFormDialog.addSaveButtonListener(e -> saveUpdatedRoom());
                roomFormDialog.addCancelButtonListener(e -> roomFormDialog.closeDialog());
            }
            roomFormDialog.setupForEdit(roomOpt.get()); // Configure dialog for editing
            roomFormDialog.showDialog();

            // After dialog closes, check if saved and refresh list
            if (roomFormDialog.isSaved()) {
                loadInitialData(); // Reload data to show changes
            }
        } else {
            LOGGER.log(Level.WARNING, "Selected room ID {0} not found in database for editing.", selectedRoomId);
            roomListView.displayErrorMessage("Could not find the selected room details.");
            loadInitialData(); // Refresh list in case it was deleted concurrently
        }
    }

    /**
     * Handles saving a new room from the RoomFormDialog.
     */
    private void saveNewRoom() {
        Room roomData = roomFormDialog.getRoomData(); // Get data (includes validation)
        if (roomData != null) {
            // Use SwingWorker for database operation
            SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return roomDAO.addRoom(roomData); // Attempt to add
                }

                @Override
                protected void done() {
                    try {
                        int newId = get();
                        if (newId > 0) {
                            LOGGER.info("New room saved successfully with ID: " + newId);
                            roomFormDialog.setSaved(true); // Mark as saved
                            roomFormDialog.closeDialog(); // Close dialog
                            // Optionally show success message via main view or dialog
                        } else {
                            LOGGER.warning("Failed to save new room (DAO returned <= 0).");
                            roomFormDialog.displayErrorMessage("Failed to save room. Check logs or ensure Room Number is unique.");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.log(Level.WARNING, "Saving new room interrupted", e);
                    } catch (ExecutionException e) {
                        LOGGER.log(Level.SEVERE, "Error saving new room", e.getCause());
                        // Check for specific SQL exceptions like duplicate key if possible
                        roomFormDialog.displayErrorMessage("Error saving room: " + e.getCause().getMessage());
                    }
                }
            };
            worker.execute();
        }
        // If roomData is null, validation failed in getRoomData(), error message already shown
    }

    /**
     * Handles saving an updated room from the RoomFormDialog.
     */
    private void saveUpdatedRoom() {
        Room roomData = roomFormDialog.getRoomData(); // Gets data, includes ID if editing
        if (roomData != null) {
            // Use SwingWorker for database operation
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return roomDAO.updateRoom(roomData); // Attempt to update
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            LOGGER.info("Room updated successfully: ID " + roomData.getRoomId());
                            roomFormDialog.setSaved(true);
                            roomFormDialog.closeDialog();
                        } else {
                            LOGGER.warning("Failed to update room: ID " + roomData.getRoomId());
                            roomFormDialog.displayErrorMessage("Failed to update room. Check logs or ensure Room Number is unique.");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.log(Level.WARNING, "Updating room interrupted", e);
                    } catch (ExecutionException e) {
                        LOGGER.log(Level.SEVERE, "Error updating room", e.getCause());
                        roomFormDialog.displayErrorMessage("Error updating room: " + e.getCause().getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Handles deactivating the selected room.
     */
    private void deleteRoom() {
        int selectedRoomId = roomListView.getSelectedRoomId();
        if (selectedRoomId < 0) {
            roomListView.displayErrorMessage("Please select a room to deactivate.");
            return;
        }

        // Confirm deactivation
        int confirmation = roomListView.displayConfirmDialog(
                "Are you sure you want to deactivate room ID " + selectedRoomId + "?\nThis will mark the room as inactive.",
                "Confirm Deactivation");

        if (confirmation == JOptionPane.YES_OPTION) {
            LOGGER.info("Attempting to deactivate room ID: " + selectedRoomId);
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return roomDAO.setActiveStatus(selectedRoomId, false);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            LOGGER.info("Room deactivated successfully: ID " + selectedRoomId);
                            roomListView.displayInfoMessage("Room has been successfully deactivated.");
                            loadInitialData(); // Refresh the list
                        } else {
                            LOGGER.warning("Failed to deactivate room: ID " + selectedRoomId);
                            roomListView.displayErrorMessage("Could not deactivate room. It may have active tenants or already be inactive.");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.log(Level.WARNING, "Deactivation interrupted", e);
                    } catch (ExecutionException e) {
                        LOGGER.log(Level.SEVERE, "Error deactivating room", e.getCause());
                        roomListView.displayErrorMessage("Error deactivating room: " + e.getCause().getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

}
