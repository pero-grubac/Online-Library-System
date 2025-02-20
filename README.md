# 📚 Online Library System

This project simulates the operation of an online library. It includes multiple applications and components  
to handle library operations, member management, book inventory, and supplier integration.  
Below is an overview of the key features and functionalities.

## ✨ Key Features  
- **Library Application:** GUI application for library staff to manage members, books, and reservations.  
- **Member Application:** GUI application for members to browse, reserve, and download books.  
- **Supplier Application:** GUI application for book suppliers to manage book inventory and handle orders.  

## 📋 Library Application  
- Manage members: Add, view, update, delete, approve, or reject member registration requests.  
- Book inventory: CRUD operations for books stored in a Redis database.  
- Handle book reservations: Approve or reject member book reservation requests.  
- Order books from suppliers using socket communication.  
- Support for test data generation during application startup.  

## 👤 Member Application  
- **Registration:** Members can register with personal details (e.g., name, address, email) via RESTful endpoints.  
- **Login:** Authenticate using a username and password.  
- **Book browsing:** View all books in a table format, with search and filtering options.  
- **Book details:** Open a popup to see the title, cover, and first 100 lines of book content.  
- **Download books:** Select books to download as a ZIP archive sent to the member's email.  
- **Chat:** Members can exchange opinions about books via a secure socket-based chat application.  
- **Multicast messaging:** Suggest new books for purchase, visible to other members and librarians.  

## 📦 Supplier Application  
- **Manage inventory:** Add and view books offered to the library.  
- **Integration with Project Gutenberg:** Automatically fetch book data and cover images using predefined links.  
- **Order handling:** Process library orders using MQ (oldest-first retrieval), with options to approve or reject orders.  
- **Invoice generation:** Generate invoices for approved orders and communicate with the accounting service via RMI.  
- **Tax calculation:** Include a 17% VAT in each invoice.  

## 📂 Technical Details  
- **Technologies:** RESTful APIs, Redis, XML, Socket Communication, Multicast, MQ, RMI.  
- **Logging:** Logger used for exception handling.  
- **Configuration:** Properties files for file paths and configurations.  
- **Serialization:** Custom serialization for invoice storage on the RMI server.  
- **Concurrency:** Supports one library application, multiple member applications, and multiple supplier applications simultaneously.  

## 🚀 How to Run  
1. Start the **LibraryServer** to handle RESTful endpoints and socket communications.  
2. Launch the **Library GUI** for staff operations.  
3. Run the **Member GUI** application for library users.  
4. Launch the **SupplierServer** and **Supplier GUI** for book suppliers.  
5. Ensure all necessary configurations are set in the properties files.  

## 📈 Future Improvements  
- Enhanced security mechanisms for user data and communication.  
- Scalability to handle larger databases and user loads.  
- Improved UI/UX for all applications.  
