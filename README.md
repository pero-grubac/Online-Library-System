<!DOCTYPE html>
<html>
<body>
    <h1>📚 Online Library System</h1>
    <p>
        This project simulates the operation of an online library. It includes multiple applications and components 
        to handle library operations, member management, book inventory, and supplier integration. 
        Below is an overview of the key features and functionalities.
    </p>
    <h2>✨ Key Features</h2>
    <ul>
        <li><strong>Library Application:</strong> GUI application for library staff to manage members, books, and reservations.</li>
        <li><strong>Member Application:</strong> GUI application for members to browse, reserve, and download books.</li>
        <li><strong>Supplier Application:</strong> GUI application for book suppliers to manage book inventory and handle orders.</li>
    </ul>
    <h2>📋 Library Application</h2>
    <ul>
        <li>Manage members: Add, view, update, delete, approve, or reject member registration requests.</li>
        <li>Book inventory: CRUD operations for books stored in a Redis database.</li>
        <li>Handle book reservations: Approve or reject member book reservation requests.</li>
        <li>Order books from suppliers using socket communication.</li>
        <li>Support for test data generation during application startup.</li>
    </ul>
    <h2>👤 Member Application</h2>
    <ul>
        <li>Registration: Members can register with personal details (e.g., name, address, email) via RESTful endpoints.</li>
        <li>Login: Authenticate using a username and password.</li>
        <li>Book browsing: View all books in a table format, with search and filtering options.</li>
        <li>Book details: Open a popup to see the title, cover, and first 100 lines of book content.</li>
        <li>Download books: Select books to download as a ZIP archive sent to the member's email.</li>
        <li>Chat: Members can exchange opinions about books via a secure socket-based chat application.</li>
        <li>Multicast messaging: Suggest new books for purchase, visible to other members and librarians.</li>
    </ul>
    <h2>📦 Supplier Application</h2>
    <ul>
        <li>Manage inventory: Add and view books offered to the library.</li>
        <li>Integration with Project Gutenberg: Automatically fetch book data and cover images using predefined links.</li>
        <li>Order handling: Process library orders using MQ (oldest-first retrieval), with options to approve or reject orders.</li>
        <li>Invoice generation: Generate invoices for approved orders and communicate with the accounting service via RMI.</li>
        <li>Tax calculation: Include a 17% VAT in each invoice.</li>
    </ul>
    <h2>📂 Technical Details</h2>
    <ul>
        <li><strong>Technologies:</strong> RESTful APIs, Redis, XML, Socket Communication, Multicast, MQ, RMI.</li>
        <li><strong>Logging:</strong> Logger used for exception handling.</li>
        <li><strong>Configuration:</strong> Properties files for file paths and configurations.</li>
        <li><strong>Serialization:</strong> Custom serialization for invoice storage on the RMI server.</li>
        <li><strong>Concurrency:</strong> Supports one library application, multiple member applications, and multiple supplier applications simultaneously.</li>
    </ul>
    <h2>🚀 How to Run</h2>
    <ol>
        <li>Start the <strong>LibraryServer</strong> to handle RESTful endpoints and socket communications.</li>
        <li>Launch the <strong>Library GUI</strong> for staff operations.</li>
        <li>Run the <strong>Member GUI</strong> application for library users.</li>
        <li>Launch the <strong>SupplierServer</strong> and <strong>Supplier GUI</strong> for book suppliers.</li>
        <li>Ensure all necessary configurations are set in the properties files.</li>
    </ol>
    <h2>📈 Future Improvements</h2>
    <ul>
        <li>Enhanced security mechanisms for user data and communication.</li>
        <li>Scalability to handle larger databases and user loads.</li>
        <li>Improved UI/UX for all applications.</li>
    </ul>
    <h2>💻 Contributions</h2>
    <p>
        Contributions are welcome! Please feel free to open an issue or submit a pull request for any improvements or new features.
    </p>
</body>
</html>
