# **Simple Bank Account Simulation**

**Overview**

This project simulates a simple bank account system where users can transfer and withdraw money using debit or credit cards. The system charges an extra 1% fee for transactions done with a credit card.

The project is built using Spring Boot 3.2 and Java 21, with PostgreSQL as the database, and is containerized using Docker. GitHub is used for version control and CI. Swagger is available for API documentation.

**Requirements & Validations**

- Negative balance is not allowed.
- Each account must contain user details, card details, and the current balance.
- REST endpoints:
    - Check current available balance in all accounts.
    - Withdraw money.
    - Transfer money.
- Each account is linked to one credit or debit card.
- All transfers and withdrawals should be auditable.


**Assumptions**

- Each user has only one account.
- No different types of accounts are considered.
- Users can choose to have a credit card or not. A debit card is mandatory.
- Credit cards are pre-paid.
- The transfer system operates within the same bank.


**Future Considerations**

- Implement mechanisms to prevent the same transaction from being persisted twice.
- Add security mechanisms to prevent brute-forcing card details via the API.
- Create REST API to retrieve transaction history.
- Define how to expose audit data present in the transaction table (log stream, UI, API, monthly reports).
- Make card creation asynchronous to avoid account creation failure.
- Enhance encryption security (consider salting).
- Improve the performance of the transaction table (e.g., partitioning by timestamp).
- Setup tracing and metrics for observability using the preferred tool (Splunk, NewRelic or other).


**Technologies Used**

- Spring Boot 3.2
- Java 21
- PostgresSQL
- Maven
- Docker
- Flyway
- GitHub
- GitHub Actions
- Swagger

**Data Base Diagram**

![img.png](.github/images/database_diagram.png)

**Getting Started**

###### **Prerequisites**

- Docker
- Java 21
- Maven

###### Running the ApplicationRunning the Application

Clone the repository:

```shell
git clone https://github.com/JhonydePaula22/simple-bank-account.git
cd simple-bank-account
```

Compile the project:

```shel
mvn clean compile
```

Run unit tests

```shel
mvn test
```

Run integration-tests

```shel
mvn verify -Pintegration-tests
```

Build the Docker image:

```shel
docker build -t simple-bank-account:v1 .
```

Required Environment Variables

```shel
DB_USER=<db-user> #default is user
DB_PASSWORD=<db-password> #default is S3cret
DB_URL=<db-url> #default is postgresql://host.docker.internal:5432/simple_bank_account_assignment'
ENCRYPTION_KEY=<encryption-key> #default 5lyi1fhGSeoBrI0+qERnWBUJmitWJ9IX3GVCYqANmt4='
```

Run the Docker image:
```shel
docker run -e DB_PASSWORD=$DB_PASSWORD -e DB_URL=$DB_URL -e DB_USER=$DB_USER \
-e ENCRYPTION_KEY=$ENCRYPTION_KEY -p 8080:8080 \
--name simple-bank-account simple-bank-account:v1
```

Start the services using Docker Compose:

The docker compose file is in the docker folder. From there you can run the following command to start it locally

```shel
DB_USER=$DB_USER DB_PASSWORD=$DB_PASSWORD DB_NAME=simple_bank_account_assignment docker compose up -d
```

###### Access Swagger Documentation:Access Swagger Documentation:

Open your browser and navigate to http://localhost:8080/v1/swagger-ui.html to explore the available REST endpoints.


**License**

This project is licensed under the MIT License.

Feel free to reach out if you have any questions or need further assistance.