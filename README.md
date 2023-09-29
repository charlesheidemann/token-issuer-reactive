# Token Issuer Service

The Token Issuer Service is a reactive microservice built using Spring Boot and WebFlux that implements the Request/Response Pattern. It is designed to handle token issuance asynchronously and efficiently using Apache Kafka for messaging and caching.

## Table of Contents

- [Introduction](#introduction)
- [Architecture](#architecture)
- [Components](#components)
- [Getting Started](#getting-started)
- [Build and Run](#build-and-run)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Introduction

The Token Issuer Service utilizes reactive programming principles to handle token issuance asynchronously. It is built to process incoming requests and generate authentication tokens based on these requests. The service is designed to be highly responsive and scalable, making it suitable for modern microservices architectures.

## Architecture

The Request/Response pattern is a widely used communication pattern in software systems, where one component (the requester) sends a request to another component (the responder) and expects a corresponding response. The Token Issuer Service utilizes this pattern to handle token issuance asynchronously.

### How it Works

1. **Client Requests Token**: An external client, such as another microservice or application, sends a request for a token to the Token Issuer Service. This request is typically in the form of a `TokenRequest` DTO.

2. **Request Processing**: Upon receiving the token request, the service assigns a unique correlation ID to it. This correlation ID helps in tracking and associating the request with its response.

3. **Token Issuance**: The service processes the received request, which may involve performing authentication, generating a token, or other relevant operations based on the request's content.

4. **Asynchronous Processing**: To avoid blocking the client and enable asynchronous processing, The Token Issuer Service utilizes reactive programming. It processes the request asynchronously, allowing it to handle multiple requests concurrently.

5. **Producing Token Response**: Once the token is successfully generated or processed, the service creates a `TokenResponse` DTO, including the generated token, and prepares to send it as a response.

6. **Sending Response to Kafka**: The service then publishes the `TokenResponse` DTO to a Kafka topic designated for responses. This topic serves as a communication channel for sending responses back to the component that initiated the request.

7. **Client Receives Response**: The client, which initially made the token request, listens to the Kafka topic for responses. When it receives a response with the matching correlation ID, it can handle the response.

### Benefits

- Non-blocking Processing: Allows the service to handle multiple token issuance requests concurrently without blocking the clients.
- Asynchronous Communication: Ensures responsiveness and scalability in a distributed system.

In summary, The Token Issuer Service Reactive's implementation of the Request/Response pattern enables seamless and non-blocking token issuance while maintaining responsiveness and scalability in a distributed system.

## Components

The Token Issuer Service follows a reactive architecture and relies on Apache Kafka for message passing. It consists of several key components, including:

- **TokenRequestProducer:** A Kafka producer responsible for sending TokenRequest messages to the Kafka cluster.

- **TokenRequestProcessor:** This component consumes incoming TokenRequest messages from Kafka, processes them to generate authentication tokens, and subsequently produces TokenResponse messages back to Kafka.

- **TokenResponseCacheProvider:** A component that manages the caching of TokenResponse objects for efficient retrieval.

- **TokenIssuerController:** A REST controller that exposes an endpoint for requesting authentication tokens.

- **TokenIssuerService:** A service layer responsible for handling TokenRequest objects and returning TokenResponse objects.

- **TokenRequest and TokenResponse:** DTOs representing incoming requests and generated responses.

## Getting Started

To get started with the Token Issuer Service, follow these steps:

1. Clone this repository to your local machine.
2. Ensure you have Java 17 and Maven installed on your system.
3. Ensure that you have a Kafka broker up and running, and that it is properly configured.
4. Configure the application properties in `src/main/resources/application.yml`, including Kafka settings, if necessary.
5. Build the project using Maven.

## Build and Run

To build and run the application:

```bash
# Clone the repository
git clone https://github.com/yourusername/token-issuer-reactive.git

# Navigate to the project directory
cd token-issuer-reactive

# Build the project using Maven
./mvnw clean package

# Run the application
java -jar target/token-issuer-reactive-0.0.1-SNAPSHOT.jar
```

## Dependencies

Token Issuer Reactive relies on the following dependencies:

- [Spring Boot Starter WebFlux](https://spring.io/projects/spring-boot): For building reactive web applications.
- [Spring Kafka](https://spring.io/projects/spring-kafka): For Kafka integration.
- [Reactor Kafka](https://projectreactor.io/docs/kafka/release/reference/): For reactive Kafka support.
- [Lombok](https://projectlombok.org/): A library for reducing boilerplate code in Java.

## Usage

To use the Token Issuer Service:

1. Ensure that you have Kafka set up and configured correctly.
2. Build and run the Token Issuer Service application.
3. Send HTTP POST requests to the `/token` endpoint with a TokenRequest to request authentication tokens. Sample Request:
```bash
curl --request POST \
--url http://localhost:8080/token \
--header 'Content-Type: application/json' \
--data '{
"user": "user",
"credential":"credential"
}'
```
4. The service will respond with a TokenResponse containing the generated token.

## Contributing

Contributions are welcome! If you have improvements, bug fixes, or new features to propose, please submit a pull request. For major changes, please open an issue first to discuss the proposed changes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.