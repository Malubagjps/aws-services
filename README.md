# AWS Textract & Rekognition Integration System

Spring Boot application that integrates AWS Textract for receipt processing and AWS Rekognition for image analysis, storing structured data into MySQL database.

## Features

### AWS Textract
- ✅ OCR and text extraction from documents
- ✅ Extract and parse receipt data (company info, items, totals)
- ✅ Store structured receipt data in MySQL using JPA

### AWS Rekognition
- ✅ Label detection (objects, scenes, activities)
- ✅ Celebrity recognition
- ✅ Intelligent image analysis

### General
- ✅ RESTful API with Swagger UI documentation
- ✅ Global exception handling
- ✅ Support for multiple image formats (PNG, JPG, PDF)

## Technologies Used

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **MySQL 8.0+**
- **AWS SDK**
    - AWS Textract
    - AWS Rekognition
- **Lombok**
- **SpringDoc OpenAPI 3 (Swagger) v2.8.13**
- **Maven**

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0+
- AWS Account with Textract and Rekognition access
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
spring.datasource.url=jdbc:mysql://localhost/receipt_db
spring.datasource.username=root
spring.datasource.password=your-password
spring.jpa.hibernate.ddl-auto=update
```

### 5. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8084`

## API Endpoints

### AWS Textract Endpoints

#### 1. Extract Raw Text
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

#### 2. Process Receipt
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

**Response:**
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
    }
  ],
  "subTotal": 107.60,
  "cash": 200.00,
  "changeAmount": 92.40
}
```

#### 3. Get All Receipts
**GET** `/api/v1/textract/receipts`

Retrieves all stored receipts.

**Example:**
```bash
curl http://localhost:8084/api/v1/textract/receipts
```

#### 4. Get Receipt by ID
**GET** `/api/v1/textract/receipts/{id}`

Retrieves specific receipt by ID.

**Example:**
```bash
curl http://localhost:8084/api/v1/textract/receipts/1
```

---

### AWS Rekognition Endpoints

#### 1. Detect Labels
**POST** `/api/v1/rekognition/labels`

Detects objects, scenes, and activities in an image.

- **Content-Type**: `multipart/form-data`
- **Parameter**: `file` (PNG, JPG)
- **Query Parameter**: `minConfidence` (optional, default: 70.0)
- **Response**: List of detected labels with confidence scores

**Example:**
```bash
curl -X POST "http://localhost:8084/api/v1/rekognition/labels?minConfidence=70.0" \
  -F "file=@image.jpg"
```

**Response:**
```json
{
  "labels": [
    {
      "name": "Person",
      "confidence": 99.8
    },
    {
      "name": "Car",
      "confidence": 95.2
    },
    {
      "name": "Street",
      "confidence": 92.5
    }
  ],
  "totalDetections": 3
}
```

#### 2. Recognize Celebrities
**POST** `/api/v1/rekognition/celebrities`

Identifies famous people in an image.

- **Content-Type**: `multipart/form-data`
- **Parameter**: `file` (PNG, JPG)
- **Response**: List of recognized celebrities with confidence and info URLs

**Example:**
```bash
curl -X POST http://localhost:8084/api/v1/rekognition/celebrities \
  -F "file=@celebrity.jpg"
```

**Response:**
```json
{
  "celebrities": [
    {
      "name": "Jeff Bezos",
      "matchConfidence": 98.7,
      "urls": [
        "www.imdb.com/name/nm1757263"
      ]
    }
  ],
  "totalDetections": 1
}
```

## Swagger UI

Access API documentation:
```
http://localhost:8084/swagger-ui.html
```

Interactive API testing available for all endpoints.

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
- **500 Internal Server Error**: AWS service errors (Textract/Rekognition) or server errors


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
│   │   ├── TextractController.java
│   │   └── RekognitionController.java
│   ├── dto/
│   │   ├── ReceiptDTO.java
│   │   ├── ReceiptItemDTO.java
│   │   ├── ImageAnalysisDTO.java
│   │   ├── DetectedLabelDTO.java
│   │   └── CelebrityDTO.java
│   ├── entity/
│   │   ├── Receipt.java
│   │   └── ReceiptItem.java
│   ├── exception/
│   │   ├── ErrorResponse.java
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ReceiptNotFoundException.java
│   │   ├── TextractException.java
│   │   └── RekognitionException.java
│   ├── record/
│   │   └── ExtractTextResponse.java
│   ├── dao/
│   │   └── ReceiptDAO.java
│   └── service/
│       ├── TextractService.java
│       ├── RekognitionService.java
│       └── impl/
│           ├── TextractServiceImpl.java
│           └── RekognitionServiceImpl.java
└── AwsTextractApplication.java
```

## Use Cases

### Textract
- Receipt digitization and expense tracking
- Invoice processing
- Form data extraction
- Document archival systems

### Rekognition
- E-commerce product auto-tagging
- Content moderation
- Media asset management
- Celebrity identification in photos
- Social media content analysis


## Developer

**Jhon Paul Malubag**
- Email: malubagjp.srbootcamp2025@gmail.com