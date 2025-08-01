# NASA Asteroid Alert System

A comprehensive system for monitoring potentially hazardous asteroids and sending email alerts to registered users.

## 🚀 Features

### Backend (Spring Boot)
- **Kafka Integration**: Consumes asteroid collision events from Kafka topics
- **Email Service**: Sends beautiful HTML emails with asteroid alerts and NASA APOD
- **User Management**: REST API for user registration and management
- **Database**: MySQL integration with JPA/Hibernate
- **Error Handling**: Robust error handling with retry mechanisms
- **Scheduling**: Automated email sending every 10 seconds

### Frontend (Angular)
- **User Registration**: Modern, responsive registration form
- **Form Validation**: Real-time validation with error messages
- **Beautiful UI**: Gradient backgrounds and smooth animations
- **Responsive Design**: Works on desktop, tablet, and mobile

## 📋 Prerequisites

- Java 17+
- Node.js 16+
- MySQL 8.0+
- Docker (for Kafka setup)

## 🛠️ Setup Instructions

### 1. Database Setup

Create a MySQL database named `asteroidalert`:

```sql
CREATE DATABASE asteroidalert;
```

### 2. Environment Variables

Create a `.env` file in the root directory:

```env
# Database
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
MYSQL_ROOT_PASSWORD=your_mysql_root_password

# Email (Mailtrap)
MAILTRAP_USERNAME=your_mailtrap_username
MAILTRAP_PASSWORD=your_mailtrap_password
MAIL_FROM_EMAIL=noreply@nasa-asteroid-alerts.com

# NASA API (Optional)
NASA_API_KEY=your_nasa_api_key
```

### 3. Start Infrastructure

Start Kafka, MySQL, and other services using Docker Compose:

```bash
docker-compose up -d
```

### 4. Backend Setup

1. Navigate to the Spring Boot project directory:
```bash
cd emailnotificationservice
```

2. Build and run the application:
```bash
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8081`

### 5. Frontend Setup

1. Navigate to the Angular project directory:
```bash
cd angular-user-registration
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm start
```

The frontend will start on `http://localhost:4200`

## 📊 API Endpoints

### User Registration
- **POST** `/api/users` - Register a new user
- **GET** `/api/users` - Get all users

### Request Format
```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "notificationEnabled": true
}
```

## 🎨 Frontend Features

### User Registration Form
- **First Name**: Required, minimum 2 characters
- **Last Name**: Required, minimum 2 characters
- **Email**: Required, valid email format
- **Notifications**: Toggle to enable/disable email alerts

### UI/UX Features
- ✅ **Real-time validation**
- ✅ **Beautiful gradient design**
- ✅ **Responsive layout**
- ✅ **Smooth animations**
- ✅ **Error handling**
- ✅ **Success feedback**

## 📧 Email Features

### HTML Email Template
- **Asteroid Alerts**: Detailed information about detected asteroids
- **Risk Assessment**: Color-coded risk levels (🔴 HIGH, 🟡 MEDIUM, 🟢 LOW)
- **NASA APOD**: Daily astronomy picture with explanation
- **Responsive Design**: Works on all email clients

### Email Content
- Asteroid name and details
- Close approach date
- Estimated diameter
- Miss distance
- Risk level assessment
- NASA's Astronomy Picture of the Day

## 🔧 Configuration

### Application Properties
```properties
# Server
server.port=8081

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/asteroidalert
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=asteroid-alert

# Email
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=${MAILTRAP_USERNAME}
spring.mail.password=${MAILTRAP_PASSWORD}

# NASA API
nasa.api.key=${NASA_API_KEY:DEMO_KEY}
```

## 🚨 Error Handling

### Backend
- **Retry Logic**: 3 attempts with exponential backoff for email sending
- **Graceful Degradation**: Continues working if APOD API fails
- **Data Validation**: Comprehensive input validation
- **Logging**: Detailed logging for debugging

### Frontend
- **Form Validation**: Real-time validation with helpful error messages
- **Network Error Handling**: Graceful handling of API failures
- **User Feedback**: Clear success/error messages

## 📱 Responsive Design

The Angular application is fully responsive:
- **Desktop**: Full-width layout with optimal spacing
- **Tablet**: Adjusted padding and font sizes
- **Mobile**: Single-column layout with touch-friendly elements

## 🔒 Security Features

- **HTML Escaping**: Prevents XSS attacks in email content
- **Input Validation**: Server-side validation of all inputs
- **CORS Configuration**: Proper CORS setup for frontend-backend communication

## 🧪 Testing

### Backend Testing
```bash
./mvnw test
```

### Frontend Testing
```bash
cd angular-user-registration
npm test
```

## 📈 Monitoring

### Logs
The application provides detailed logging:
- Email sending status
- User registration events
- Error tracking
- Performance metrics

### Metrics
- Email success/failure rates
- User registration counts
- API response times

## 🚀 Deployment

### Backend Deployment
1. Build the JAR file:
```bash
./mvnw clean package
```

2. Run the application:
```bash
java -jar target/emailnotificationservice-0.0.1-SNAPSHOT.jar
```

### Frontend Deployment
1. Build the production version:
```bash
cd angular-user-registration
npm run build
```

2. Deploy the `dist` folder to your web server

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support, please open an issue in the GitHub repository or contact the development team.

---

**Note**: This system is designed for educational and demonstration purposes. For production use, additional security measures and monitoring should be implemented. 