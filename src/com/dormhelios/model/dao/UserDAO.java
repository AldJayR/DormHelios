package com.dormhelios.model.dao;

import com.dormhelios.model.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entities.
 */
public interface UserDAO {

    /**
     * Finds a user by their unique username. Used primarily for login
     * authentication.
     *
     * @param username The username to search for.
     * @return An Optional containing the User if found, otherwise empty.
     */
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their ID.
     *
     * @param userId The ID of the user.
     * @return An Optional containing the User if found, otherwise empty.
     */
    Optional<User> findById(int userId);

    /**
     * Retrieves all users from the database. Use with caution, might return
     * many users. Consider filtering or pagination.
     *
     * @return A List of all User objects.
     */
    List<User> findAll();

    /**
     * Adds a new user to the database. The user object should have its ID set
     * upon successful insertion if auto-generated.
     *
     * @param user The User object to add (without ID).
     * @return The generated ID of the newly added user, or -1 if failed.
     */
    int addUser(User user); // Return generated ID

    /**
     * Updates an existing user's details in the database. Assumes
     * user.getUserId() is set and valid.
     *
     * @param user The User object with updated information.
     * @return true if the update was successful, false otherwise.
     */
    boolean updateUser(User user);

    /**
     * Deletes a user from the database by their ID. Consider soft delete
     * (setting is_active=false) instead of hard delete.
     *
     * @param userId The ID of the user to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    boolean deleteUser(int userId); // Or implement soft delete: boolean setActiveStatus(int userId, boolean isActive);
}
