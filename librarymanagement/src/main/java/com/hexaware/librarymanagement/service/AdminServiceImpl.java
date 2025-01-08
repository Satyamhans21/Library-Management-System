package com.hexaware.librarymanagement.service;

import com.hexaware.librarymanagement.dto.AdminDTO;
import com.hexaware.librarymanagement.dto.AdminNotificationDTO;
import com.hexaware.librarymanagement.entity.Admin;
import com.hexaware.librarymanagement.entity.AdminNotification;
import com.hexaware.librarymanagement.entity.User;
import com.hexaware.librarymanagement.exception.CRUDAPIException;
import com.hexaware.librarymanagement.mapper.AdminMapper;
import com.hexaware.librarymanagement.repository.AdminNotificationRepository;
import com.hexaware.librarymanagement.repository.AdminRepository;
import com.hexaware.librarymanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements IAdminService {

    // Injecting required repositories for database interactions
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminNotificationRepository adminNotificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Adds a new admin to the system.
     * Validates the required fields before saving.
     *
     * @param adminDTO the admin data transfer object containing admin details.
     * @return the saved admin details as AdminDTO.
     */
    @Override
    public AdminDTO addAdmin(AdminDTO adminDTO) {
        if (adminDTO.getEmail() == null || adminDTO.getAdminName() == null) {
            throw new CRUDAPIException(HttpStatus.BAD_REQUEST, "Invalid Admin Data",
                    "Admin name and email are required.");
        }

        // Convert AdminDTO to Admin entity
        Admin admin = AdminMapper.mapToAdmin(adminDTO);
        Admin savedAdmin = adminRepository.save(admin);

        // Convert saved Admin entity back to AdminDTO and return
        return AdminMapper.mapToAdminDTO(savedAdmin);
    }

    /**
     * Retrieves a list of all admins in the system.
     *
     * @return list of all AdminDTOs.
     */
    @Override
    public List<AdminDTO> getAllAdmins() {
        List<Admin> admins = adminRepository.findAll();
        if (admins.isEmpty()) {
            throw new CRUDAPIException(HttpStatus.NOT_FOUND, "No Admins Found",
                    "There are no admins in the system.");
        }

        // Convert List<Admin> to List<AdminDTO>
        return admins.stream()
                .map(AdminMapper::mapToAdminDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves details of an admin by their ID.
     *
     * @param id the ID of the admin to retrieve.
     * @return the admin details as AdminDTO.
     */
    @Override
    public AdminDTO getAdminById(int id) {
        Optional<Admin> admin = adminRepository.findById(id);
        if (admin.isEmpty()) {
            throw new CRUDAPIException(HttpStatus.NOT_FOUND, "Admin Not Found",
                    "Admin with ID " + id + " does not exist.");
        }
        return AdminMapper.mapToAdminDTO(admin.get());
    }

    /**
     * Deletes an admin from the system by their ID.
     *
     * @param id the ID of the admin to delete.
     */
    @Override
    public void deleteAdminById(int id) {
        if (!adminRepository.existsById(id)) {
            throw new CRUDAPIException(HttpStatus.NOT_FOUND, "Admin Not Found",
                    "Admin with ID " + id + " does not exist.");
        }
        adminRepository.deleteById(id);
    }

    /**
     * Sends a notification from an admin to a user.
     *
     * @param adminId  the ID of the admin sending the notification.
     * @param userId   the ID of the user receiving the notification.
     * @param message  the message content.
     * @return the saved notification as AdminNotificationDTO.
     */
    public AdminNotificationDTO sendNotificationToUser(int adminId, int userId, String message) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "Admin Not Found",
                        "Admin with ID " + adminId + " does not exist."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "User Not Found",
                        "User with ID " + userId + " does not exist."));

        AdminNotification notification = new AdminNotification();
        notification.setAdmin(admin);
        notification.setUser(user);
        notification.setMessage(message);
        notification.setSentDate(new Date());

        // Save the notification entity
        AdminNotification savedNotification = adminNotificationRepository.save(notification);

        // Map the saved notification entity to AdminNotificationDTO and return
        return AdminMapper.mapToAdminNotificationDTO(savedNotification);
    }

    /**
     * Retrieves all notifications sent by a specific admin.
     *
     * @param adminId the ID of the admin.
     * @return list of AdminNotificationDTOs sent by the admin.
     */
    public List<AdminNotificationDTO> getNotificationsByAdmin(int adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "Admin Not Found",
                        "Admin with ID " + adminId + " does not exist."));

        List<AdminNotification> notifications = adminNotificationRepository.findByAdmin(admin);
        if (notifications.isEmpty()) {
            throw new CRUDAPIException(HttpStatus.NOT_FOUND, "No Notifications Found",
                    "No notifications found for admin with ID " + adminId);
        }

        return notifications.stream()
                .map(AdminMapper::mapToAdminNotificationDTO)  // Map each AdminNotification entity to AdminNotificationDTO
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all notifications received by a specific user.
     *
     * @param userId the ID of the user.
     * @return list of AdminNotificationDTOs received by the user.
     */
    public List<AdminNotificationDTO> getNotificationsByUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "User Not Found",
                        "User with ID " + userId + " does not exist."));

        List<AdminNotification> notifications = adminNotificationRepository.findByUser(user);
        if (notifications.isEmpty()) {
            throw new CRUDAPIException(HttpStatus.NOT_FOUND, "No Notifications Found",
                    "No notifications found for user with ID " + userId);
        }

        return notifications.stream()
                .map(AdminMapper::mapToAdminNotificationDTO)  // Map each AdminNotification entity to AdminNotificationDTO
                .collect(Collectors.toList());
    }

    /**
     * Deletes a specific notification by its ID.
     *
     * @param notificationId the ID of the notification to delete.
     */
    public void deleteNotification(int notificationId) {
        AdminNotification notification = adminNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "Notification Not Found",
                        "Notification with ID " + notificationId + " does not exist."));

        adminNotificationRepository.delete(notification);
    }

    /**
     * Retrieves the details of a specific notification by its ID.
     *
     * @param notificationId the ID of the notification.
     * @return the notification details as AdminNotificationDTO.
     */
    public AdminNotificationDTO getNotificationDetails(int notificationId) {
        AdminNotification notification = adminNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new CRUDAPIException(HttpStatus.NOT_FOUND, "Notification Not Found",
                        "Notification with ID " + notificationId + " does not exist."));

        return AdminMapper.mapToAdminNotificationDTO(notification);  // Map the found notification entity to DTO
    }
}
