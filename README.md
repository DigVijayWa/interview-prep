# interview-prep

A comprehensive Java project for preparing Data Structures & Algorithms (DSA) and Low-Level Design (LLD) interview questions. This repository contains well-structured code examples, reusable utilities, and runnable demos to help you practice and master key concepts for technical interviews.

## Table of Contents

- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [How to Run](#how-to-run)
- [Contents](#contents)
  - [DSA](#dsa)
  - [Low-Level Design](#low-level-design)
- [Contributing](#contributing)
- [License](#license)

---

## Project Structure

```
preparation/
  pom.xml
  src/
    main/
      java/
        preparation/
          dsa/
            DSARunner.java
            RemoveDuplicates.java
            TwoSum.java
          low_level_design/
            KeyValueStore.java
            RateLimiter.java
    test/
      java/
        preparation/
          dsa/
target/
  preparation-1.0-SNAPSHOT.jar
```

- `dsa/`: Data Structures and Algorithms problems and solutions.
- `low_level_design/`: Low-Level Design patterns and implementations.
- `test/`: Unit tests for DSA and LLD modules.

---

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.x

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/interview-prep.git
   cd interview-prep/preparation
   ```

2. Build the project using Maven:
   ```bash
   mvn clean install
   ```

---

## How to Run

### Run All DSA Demos

```bash
mvn exec:java -Dexec.mainClass="preparation.dsa.DSARunner"
```

### Run a Specific Class

You can run any class with a `main` method, for example:

```bash
mvn exec:java -Dexec.mainClass="preparation.dsa.TwoSum"
```

---

## Contents

### DSA

- `TwoSum.java`: Classic Two Sum problem.
- `RemoveDuplicates.java`: Remove duplicates from arrays.
- `DSARunner.java`: Entry point to run DSA demos.

### Low-Level Design

- `KeyValueStore.java`: Simple in-memory key-value store.
- `RateLimiter.java`: Basic rate limiter implementation.

---

## Contributing

Contributions are welcome! Please open issues or submit pull requests for new problems, solutions, or improvements.

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Commit your changes.
4. Push to your fork and submit a pull request.

---

## License

This project is licensed under the MIT License. See the [LICENSE](../LICENSE) file for details.

---