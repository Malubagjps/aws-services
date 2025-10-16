# AWS Textract Receipt Processing System

Spring Boot application that integrates AWS Textract to extract and parse text data from receipt images, storing structured data into MySQL database.

## Features

- ✅ AWS Textract integration for OCR
- ✅ Extract and parse receipt data (company info, items, totals)
- ✅ Store structured data in MySQL using JPA
- ✅ RESTful API with Swagger UI documentation
- ✅ Global exception handling
- ✅ Support for multiple image formats (PNG, JPG, PDF)

## Technologies Used

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **MySQL 8.0+**
- **AWS SDK for Java (Textract) v2.34.9**
- **Lombok**
- **SpringDoc OpenAPI 3 (Swagger) v2.8.13**
- **Maven**

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0+
- AWS Account with Textract access
- AWS credentials (Access Key & Secret Key)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd aws-textract
```

### 2. Configure MySQL Database

Create a database:
```sql
CREATE DATABASE receipt_db;
```


### 3. Configure Database Connection

Update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:8084/receipt_db
spring.datasource.username=root
spring.datasource.password=your-password
spring.jpa.hibernate.ddl-auto=update
```

### 4. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8084`

## API Endpoints

### 1. Extract Raw Text
**POST** `/api/v1/textract/extract`

Extracts raw text lines from uploaded image.

- **Content-Type**: `multipart/form-data`
- **Parameter**: `file` (PNG, JPG, or PDF)
- **Response**: List of extracted text lines

**Example:**
```bash
curl -X POST http://localhost:8084/api/v1/textract/extract \
  -F "file=@receipt.jpg"
```

### 2. Process Receipt
**POST** `/api/v1/textract/receipts/process`

Extracts, parses, and saves receipt data to database.

- **Content-Type**: `multipart/form-data`
- **Parameter**: `file` (PNG, JPG, or PDF)
- **Response**: Parsed receipt DTO with ID

**Example:**
```bash
curl -X POST http://localhost:8084/api/v1/textract/receipts/process \
  -F "file=@receipt.jpg"
```

### 3. Get All Receipts
**GET** `/api/v1/textract/receipts`

Retrieves all stored receipts.

**Example:**
```bash
curl http://localhost:8084/api/v1/textract/receipts
```

### 4. Get Receipt by ID
**GET** `/api/v1/textract/receipts/{id}`

Retrieves specific receipt by ID.

**Example:**
```bash
curl http://localhost:8084/api/v1/textract/receipts/1
```

## Swagger UI

Access API documentation:
```
http://localhost:8084/swagger-ui.html
```

## JSON Response Format

```json
{
  "id": 1,
  "companyName": "SM HYPERMARKET",
  "branch": "Quezon City",
  "managerName": "Eric Steer",
  "cashierNumber": "#3",
  "items": [
    {
      "productName": "Apple",
      "quantity": 1,
      "price": 9.20
    },
    {
      "productName": "Gardenia",
      "quantity": 1,
      "price": 19.20
    }
  ],
  "subTotal": 107.60,
  "cash": 200.00,
  "changeAmount": 92.40
}
```

## Database Schema

### receipts table
| Column | Type         | Constraint |
|--------|--------------|-----------|
| id | BIGINT       | PRIMARY KEY, AUTO_INCREMENT |
| company_name | VARCHAR(255) | |
| branch | VARCHAR(255) | |
| manager_name | VARCHAR(255) | |
| cashier_number | VARCHAR(255) | |
| sub_total | DOUBLE       | |
| cash | DOUBLE       | |
| change_amount | DOUBLE       | |

### receipt_items table
| Column | Type | Constraint |
|--------|------|-----------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| product_name | VARCHAR(255) | |
| quantity | INT | |
| price | DOUBLE | |
| receipt_id | BIGINT | FOREIGN KEY |

## Error Handling

The application handles the following errors:

- **400 Bad Request**: Invalid file format or parsing error
- **404 Not Found**: Receipt not found
- **500 Internal Server Error**: Textract processing or server errors

All errors return a structured error response with message and timestamp.

## Receipt Parsing Strategy

The parser uses semantic field detection and flexible pattern matching to extract receipt data:

- **Header Info**: Detects company name, branch, manager, and cashier using keyword matching
- **Items Section**: Identifies items boundaries and extracts product name, quantity, and price
- **Financial Data**: Extracts subtotal, cash, and change amounts from anywhere in the document

This approach tolerates varying receipt formats and OCR variations.

## Project Structure

```
src/main/java/com/srllc/aws_textract/
├── config/
│   ├── AwsConfig.java
│   └── OpenApiConfig.java
├── domain/
│   ├── controller/
│   │   └── TextractController.java
│   ├── dto/
│   │   ├── ReceiptDTO.java
│   │   └── ReceiptItemDTO.java
│   ├── entity/
│   │   ├── Receipt.java
│   │   └── ReceiptItem.java
│   ├── exception/
│   │   ├── ErrorResponse.java
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ReceiptNotFoundException.java
│   │   └── TextractException.java
│   ├── record/
│   │   └── ExtractTextResponse.java
│   ├── dao/
│   │   └── ReceiptDAO.java
│   └── service/
│       ├── TextractService.java
│       └── impl/
│           └── TextractServiceImpl.java
└── AwsTextractApplication.java
```


## Developer

**Jhon Paul Malubag**
- Email: malubagjp.srbootcamp2025@gmail.com

