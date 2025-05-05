# TestIT Reports

TestIT Reports is a web application that collects and displays statistics from the TestIT test management system. It provides insights into test case creation, modification, and execution by different users.

## Features

- **User Management**: Create, update, and delete users with different roles (USER, ADMIN)
- **Project Management**: Add projects from TestIT, manage visibility, and track statistics
- **Statistics Collection**: Automatically collect statistics from TestIT API on a scheduled basis
- **Statistics Visualization**: View statistics for test cases and test runs by user and date
- **API Integration**: Seamless integration with TestIT API
- **Authentication**: Secure JWT-based authentication

## Architecture

### Backend

- **Java 17** with **Spring Boot 3.1.5**
- **Spring Security** for authentication and authorization
- **Spring Data JPA** for database access
- **PostgreSQL** for data storage
- **Flyway** for database migrations
- **JWT** for authentication
- **WebFlux** for reactive API client

### Frontend

- **React 18** with **TypeScript**
- **Material-UI** for UI components
- **React Router** for routing
- **Axios** for API requests
- **Formik** and **Yup** for form validation
- **Chart.js** for data visualization

### Database Schema

- **Users**: Store user information and authentication details
- **Projects**: Store project information from TestIT
- **User-Project Relationships**: Track which users have access to which projects
- **Test Case Statistics**: Store statistics about test case creation and modification
- **Test Run Statistics**: Store statistics about test execution results
- **Global Settings**: Store application-wide settings

## API Endpoints

### Authentication

- `POST /api/auth/login`: Login user
- `POST /api/auth/register`: Register new user

### Users

- `GET /api/users/me`: Get current user
- `PUT /api/users/me`: Update current user
- `PUT /api/users/me/token`: Update current user's TestIT token
- `GET /api/users`: Get all users (admin only)
- `GET /api/users/{id}`: Get user by ID (admin only)
- `POST /api/users`: Create user (admin only)
- `PUT /api/users/{id}`: Update user (admin only)
- `DELETE /api/users/{id}`: Delete user (admin only)

### Projects

- `GET /api/projects`: Get all projects visible to current user
- `GET /api/projects/all`: Get all projects assigned to current user (visible and hidden)
- `GET /api/projects/{id}`: Get project by ID
- `POST /api/projects/add`: Add project from TestIT
- `PUT /api/projects/{id}/visibility`: Update project visibility for current user
- `DELETE /api/projects/{id}/remove`: Remove project from current user
- `PUT /api/projects/{id}/status`: Update project status (admin only)
- `DELETE /api/projects/{id}`: Delete project (admin only)

### Statistics

- `GET /api/statistics/projects/{projectId}`: Get statistics for a project
- `POST /api/statistics/projects/{projectId}/collect`: Manually collect statistics for a project
- `POST /api/statistics/collect-all`: Manually collect statistics for all projects (admin only)

### Admin

- `GET /api/admin/settings`: Get all global settings
- `GET /api/admin/settings/{key}`: Get global setting by key
- `PUT /api/admin/settings/{key}`: Update global setting
- `POST /api/admin/settings`: Create or update global setting
- `PUT /api/admin/settings/global-token`: Update global TestIT token
- `PUT /api/admin/settings/api-schedule`: Update API schedule cron expression
- `PUT /api/admin/settings/api-base-url`: Update API base URL

## Setup and Configuration

### Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher
- Node.js 16 or higher
- npm 8 or higher

### Configuration

The application can be configured using the following properties in `application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/testit_reports
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000

# TestIT API Configuration
testit.api.base-url=https://your-testit-instance.com/api/v2
testit.api.scheduler.cron=0 0 1 * * ?  # Run at 1:00 AM every day
```

### Running the Application

#### Backend

1. Clone the repository
2. Configure the database connection in `application.properties`
3. Run the backend application using the provided script:

```bash
./run.sh
```

Or using Maven directly:

```bash
mvn spring-boot:run
```

#### Frontend

1. Navigate to the frontend directory
2. Install dependencies and start the development server:

```bash
cd frontend
npm install
npm start
```

Or use the provided script:

```bash
cd frontend
./run-frontend.sh
```

### Docker Deployment

You can also run the entire application using Docker Compose:

```bash
./run-docker.sh
```

## Usage

1. Register an admin user
2. Login with the admin user
3. Configure global settings (TestIT API URL, global token, etc.)
4. Create regular users
5. Add projects from TestIT
6. View statistics for projects

## Project Structure

### Backend

```
src/main/java/com/testit/reports/
├── client/           # TestIT API client
├── controller/       # REST controllers
├── model/            # Entity classes
├── repository/       # Data access layer
├── security/         # Security configuration
├── service/          # Business logic
└── TestItReportsApplication.java
```

### Frontend

```
frontend/
├── public/           # Static files
└── src/
    ├── components/   # Reusable UI components
    ├── context/      # React context providers
    ├── hooks/        # Custom React hooks
    ├── pages/        # Page components
    ├── services/     # API services
    └── utils/        # Utility functions
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
