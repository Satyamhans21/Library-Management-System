package com.hexaware.librarymanagement;

import com.hexaware.librarymanagement.dto.*;
import com.hexaware.librarymanagement.entity.Book;
import com.hexaware.librarymanagement.exception.BadRequestException;
import com.hexaware.librarymanagement.exception.CRUDAPIException;
import com.hexaware.librarymanagement.mapper.BookMapper;
import com.hexaware.librarymanagement.repository.*;
import com.hexaware.librarymanagement.security.JwtTokenProvider;
import com.hexaware.librarymanagement.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.Assertions.assertThat;

import com.hexaware.librarymanagement.entity.Admin;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.hexaware.librarymanagement.entity.Role;
import com.hexaware.librarymanagement.entity.User;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class LibraryTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowedBookRepository borrowedBookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterDTO registerDTO;

    @InjectMocks
    private UserServiceImpl userService;

    @InjectMocks
    private BorrowedBookServiceImpl borrowedBookService;

    private BorrowedBookDTO borrowedBookDTO;
    private User user;
    private Book book;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Clean up database before each test
        adminRepository.deleteAll();
        bookRepository.deleteAll();
        borrowedBookRepository.deleteAll();
        categoryRepository.deleteAll();
        roleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void contextLoads() {
        assertThat(adminRepository).isNotNull();
        assertThat(bookRepository).isNotNull();
        assertThat(borrowedBookRepository).isNotNull();
        assertThat(categoryRepository).isNotNull();
        assertThat(roleRepository).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    void register_shouldReturnSuccess_whenValidUser() {
        // Arrange
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPassword");
        Role role = new Role();
        role.setName("ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(java.util.Optional.of(role));

        // Act
        String response = authService.register(registerDTO);

        // Assert
        assertEquals("Registration Successful!", response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldThrowBadRequestException_whenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.register(registerDTO);
        });
        assertEquals("Username already exists.", exception.getMessage());
    }

    @Test
    void register_shouldThrowBadRequestException_whenEmailExists() {
        // Arrange
        when(userRepository.existsByUsername(registerDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerDTO.getEmail())).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.register(registerDTO);
        });
        assertEquals("Email already exists.", exception.getMessage());
    }

    @Test
    public void testGetAllAdmins() {
        Admin admin1 = new Admin();
        admin1.setAdminName("Admin One");
        admin1.setEmail("admin1@example.com");

        Admin admin2 = new Admin();
        admin2.setAdminName("Admin Two");
        admin2.setEmail("admin2@example.com");

        when(adminRepository.findAll()).thenReturn(Arrays.asList(admin1, admin2));

        List<AdminDTO> result = adminService.getAllAdmins();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Admin One", result.get(0).getAdminName());
        assertEquals("Admin Two", result.get(1).getAdminName());

        verify(adminRepository, times(1)).findAll();
    }

    @Test
    public void testGetAdminById() {
        int adminId = 1;
        Admin admin = new Admin();
        admin.setAdminId(adminId);
        admin.setAdminName("John Doe");
        admin.setEmail("john.doe@example.com");

        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        AdminDTO result = adminService.getAdminById(adminId);

        assertNotNull(result);
        assertEquals(adminId, result.getAdminId());
        assertEquals("John Doe", result.getAdminName());

        verify(adminRepository, times(1)).findById(adminId);
    }

    @Test
    public void testAddBook() {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("Test Book");
        bookDTO.setAuthor("Test Author");
        bookDTO.setIsbn("1234567890");
        bookDTO.setPublisher("Test Publisher");
        bookDTO.setPublicationDate(new Date());
        bookDTO.setEdition("1st");
        bookDTO.setGenre("Fiction");
        bookDTO.setDescription("Test Description");
        bookDTO.setLanguage("English");
        bookDTO.setNumberOfPages(300);
        bookDTO.setCost(19.99);
        bookDTO.setAvailable(true);

        Book book = BookMapper.mapToBook(bookDTO);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDTO result = bookService.addBook(bookDTO);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        assertEquals("Test Author", result.getAuthor());
        assertEquals("1234567890", result.getIsbn());
        assertEquals("Test Publisher", result.getPublisher());
        assertEquals("1st", result.getEdition());
        assertEquals("Fiction", result.getGenre());
        assertEquals("Test Description", result.getDescription());
        assertEquals("English", result.getLanguage());
        assertEquals(300, result.getNumberOfPages());
        assertEquals(19.99, result.getCost());
        assertTrue(result.isAvailable());

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    public void testGetAllBooks() {
        Book book1 = new Book();
        book1.setTitle("Book One");
        book1.setAuthor("Author One");
        book1.setIsbn("1234567890");
        book1.setPublisher("Publisher One");
        book1.setEdition("1st");
        book1.setGenre("Fiction");
        book1.setDescription("Description One");
        book1.setLanguage("English");
        book1.setNumberOfPages(200);
        book1.setCost(9.99);
        book1.setAvailable(true);

        Book book2 = new Book();
        book2.setTitle("Book Two");
        book2.setAuthor("Author Two");
        book2.setIsbn("0987654321");
        book2.setPublisher("Publisher Two");
        book2.setEdition("2nd");
        book2.setGenre("Non-Fiction");
        book2.setDescription("Description Two");
        book2.setLanguage("Spanish");
        book2.setNumberOfPages(300);
        book2.setCost(14.99);
        book2.setAvailable(true);

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<BookDTO> result = bookService.getAllBooks();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Book One", result.get(0).getTitle());
        assertEquals("Book Two", result.get(1).getTitle());

        verify(bookRepository, times(1)).findAll();
    }

    @Test
    public void testGetBooksByAuthor() {
        Book book1 = new Book();
        book1.setTitle("Book One");
        book1.setAuthor("Author One");
        book1.setIsbn("1234567890");
        book1.setPublisher("Publisher One");
        book1.setEdition("1st");
        book1.setGenre("Fiction");
        book1.setDescription("Description One");
        book1.setLanguage("English");
        book1.setNumberOfPages(200);
        book1.setCost(9.99);
        book1.setAvailable(true);

        Book book2 = new Book();
        book2.setTitle("Book Two");
        book2.setAuthor("Author One");
        book2.setIsbn("0987654321");
        book2.setPublisher("Publisher Two");
        book2.setEdition("2nd");
        book2.setGenre("Non-Fiction");
        book2.setDescription("Description Two");
        book2.setLanguage("Spanish");
        book2.setNumberOfPages(300);
        book2.setCost(14.99);
        book2.setAvailable(true);

        when(bookRepository.findByAuthor("Author One")).thenReturn(Arrays.asList(book1, book2));

        List<BookDTO> result = bookService.getBooksByAuthor("Author One");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Author One", result.get(0).getAuthor());
        assertEquals("Author One", result.get(1).getAuthor());

        verify(bookRepository, times(1)).findByAuthor("Author One");
    }

    @Test
    public void testGetBooksByTitle() {
        Book book1 = new Book();
        book1.setTitle("Test Book");
        book1.setAuthor("Test Author");
        book1.setIsbn("1234567890");
        book1.setPublisher("Test Publisher");
        book1.setEdition("1st");
        book1.setGenre("Fiction");
        book1.setDescription("Test Description");
        book1.setLanguage("English");
        book1.setNumberOfPages(300);
        book1.setCost(19.99);
        book1.setAvailable(true);

        when(bookRepository.findByTitleContaining("Test Book")).thenReturn(Arrays.asList(book1));

        List<BookDTO> result = bookService.searchBooksByTitle("Test Book");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());

        verify(bookRepository, times(1)).findByTitleContaining("Test Book");
    }

    @Test
    public void testGetBooksByGenre() {
        Book book1 = new Book();
        book1.setTitle("Fantasy Book");
        book1.setGenre("Fantasy");
        book1.setIsbn("1234567890");
        book1.setPublisher("Fantasy Publisher");
        book1.setEdition("1st");
        book1.setDescription("Fantasy Description");
        book1.setLanguage("English");
        book1.setNumberOfPages(250);
        book1.setCost(20.00);
        book1.setAvailable(true);

        Book book2 = new Book();
        book2.setTitle("Adventure Book");
        book2.setGenre("Fantasy");
        book2.setIsbn("0987654321");
        book2.setPublisher("Adventure Publisher");
        book2.setEdition("2nd");
        book2.setDescription("Adventure Description");
        book2.setLanguage("English");
        book2.setNumberOfPages(350);
        book2.setCost(25.00);
        book2.setAvailable(true);

        when(bookRepository.findByGenre("Fantasy")).thenReturn(Arrays.asList(book1, book2));

        List<BookDTO> result = bookService.getBooksByGenre("Fantasy");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Fantasy", result.get(0).getGenre());
        assertEquals("Fantasy", result.get(1).getGenre());

        verify(bookRepository, times(1)).findByGenre("Fantasy");
    }

    @Test
    public void testGetBookById() {
        int bookId = 1;
        Book book = new Book();
        book.setBookId(bookId);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setPublisher("Test Publisher");
        book.setEdition("1st");
        book.setGenre("Fiction");
        book.setDescription("Test Description");
        book.setLanguage("English");
        book.setNumberOfPages(300);
        book.setCost(19.99);
        book.setAvailable(true);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        BookDTO result = bookService.getBookById(bookId);

        assertNotNull(result);
        assertEquals(bookId, result.getBookId());
        assertEquals("Test Book", result.getTitle());
        assertEquals("Test Author", result.getAuthor());

        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    public void testUpdateBook() {
        int bookId = 1;
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("Updated Book");
        bookDTO.setAuthor("Updated Author");
        bookDTO.setIsbn("1122334455");
        bookDTO.setPublisher("Updated Publisher");
        bookDTO.setEdition("2nd");
        bookDTO.setGenre("Updated Genre");
        bookDTO.setDescription("Updated Description");
        bookDTO.setLanguage("French");
        bookDTO.setNumberOfPages(400);
        bookDTO.setCost(22.99);
        bookDTO.setAvailable(false);

        Book existingBook = new Book();
        existingBook.setBookId(bookId);
        existingBook.setTitle("Old Book");
        existingBook.setAuthor("Old Author");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        BookDTO result = bookService.updateBook(bookId, bookDTO);

        assertNotNull(result);
        assertEquals("Updated Book", result.getTitle());
        assertEquals("Updated Author", result.getAuthor());

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    public void testDeleteBook() {
        int bookId = 1;
        Book book = new Book();
        book.setBookId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        bookService.deleteBook(bookId);

        verify(bookRepository, times(1)).delete(any(Book.class));
    }

    @Test
    public void testGetUserById_Success() {
        int userId = 1;
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("testuser@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("testuser@example.com", result.getEmail());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void testGetUserById_UserNotFound() {
        int userId = 1;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CRUDAPIException exception = assertThrows(CRUDAPIException.class, () -> userService.getUserById(userId));

        assertEquals("User Not Found", exception.getMessage());  // Updated to getMessage()
        assertEquals("User with ID 1 does not exist.", exception.getMessage());  // Updated to getMessage()
    }

    @Test
    public void testGetUserById_InvalidUserId() {
        int userId = -1;

        CRUDAPIException exception = assertThrows(CRUDAPIException.class, () -> userService.getUserById(userId));

        assertEquals("Invalid User ID", exception.getMessage());  // Updated to getMessage()
        assertEquals("User ID must be a positive number.", exception.getMessage());  // Updated to getMessage()
    }

    @Test
    public void testGetAllUsers_Success() {
        User user1 = new User();
        user1.setName("User One");
        user1.setEmail("userone@example.com");

        User user2 = new User();
        user2.setName("User Two");
        user2.setEmail("usertwo@example.com");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("User One", result.get(0).getName());
        assertEquals("User Two", result.get(1).getName());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testGetAllUsers_NoUsersFound() {
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        CRUDAPIException exception = assertThrows(CRUDAPIException.class, () -> userService.getAllUsers());

        assertEquals("No Users Found", exception.getMessage());  // Updated to getMessage()
        assertEquals("There are no users in the system.", exception.getMessage());  // Updated to getMessage()
    }


}
