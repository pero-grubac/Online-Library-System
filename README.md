<h1 align="center">📚 Online Library System</h1>

<p align="center">
  <b>A multi-component Java system that simulates an online library platform with real-time communication and integration.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-Used-red?logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/WebSockets-Enabled-blue?logo=websocket" />
  <img src="https://img.shields.io/badge/REST%20API-Available-brightgreen?logo=apachespark" />
  <img src="https://img.shields.io/badge/RabbitMQ-Used-orange?logo=rabbitmq" />
  <img src="https://img.shields.io/badge/RMI-Supported-yellow?logo=protocols" />
</p>


<hr>

<h2>✨ Key Features</h2>
<ul>
  <li><strong>Library Application:</strong> GUI application for staff to manage members, books, and reservations.</li>
  <li><strong>Member Application:</strong> GUI for members to browse, reserve, and download books.</li>
  <li><strong>Supplier Application:</strong> GUI for suppliers to handle inventory and book orders.</li>
</ul>

<h2>📋 Library Application</h2>
<ul>
  <li>Manage members: approve, reject, add, update, or delete member data.</li>
  <li>Book inventory: CRUD operations using Redis as the database.</li>
  <li>Handle book reservations with review workflow.</li>
  <li>Order books from suppliers using socket-based communication.</li>
  <li>Test data is generated at startup for demonstration purposes.</li>
</ul>

<h2>👤 Member Application</h2>
<ul>
  <li><strong>Registration:</strong> Members register via REST endpoints with personal details.</li>
  <li><strong>Login:</strong> Simple authentication system using username/password.</li>
  <li><strong>Book browsing:</strong> Table view with filtering/search options.</li>
  <li><strong>Book details:</strong> View cover and preview (first 100 lines).</li>
  <li><strong>Download books:</strong> Select books to receive via email as ZIP.</li>
  <li><strong>Chat:</strong> Secure socket-based chat system between members.</li>
  <li><strong>Multicast:</strong> Suggest books for purchase visible to other users.</li>
</ul>

<h2>📦 Supplier Application</h2>
<ul>
  <li>Manage own inventory and book offerings to the library.</li>
  <li>Connects to Project Gutenberg for automatic book fetching.</li>
  <li>Process orders received from library via message queue (FIFO).</li>
  <li>Generate invoices and send them to the accounting system using RMI.</li>
  <li>Applies 17% VAT in invoice totals.</li>
</ul>

<h2>📂 Technical Details</h2>
<ul>
  <li><strong>Technologies:</strong> Java, REST APIs, Redis, XML, WebSockets, Multicast, RabbitMQ, RMI.</li>
  <li><strong>Logging:</strong> Built-in logger handles exception recording.</li>
  <li><strong>Configurations:</strong> Properties files used for paths and ports.</li>
  <li><strong>Serialization:</strong> Custom serialization used for invoice data.</li>
  <li><strong>Concurrency:</strong> Supports one library app, multiple member and supplier apps running concurrently.</li>
</ul>

<h2>🚀 How to Run</h2>
<ol>
  <li>Start the <strong>LibraryServer</strong> (handles REST and sockets).</li>
  <li>Launch the <strong>Library GUI</strong> application for internal operations.</li>
  <li>Run the <strong>Member GUI</strong> for end-user interaction.</li>
  <li>Start <strong>SupplierServer</strong> and <strong>Supplier GUI</strong> to simulate suppliers.</li>
  <li>Ensure correct paths and configurations are loaded via .properties files.</li>
</ol>
