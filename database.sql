-- ========== CREATE DATABASE ========== 
CREATE DATABASE SkyTrack;
GO
USE SkyTrack;
GO

-- ========== TABLES ========== 
CREATE TABLE Users (
    userID INT IDENTITY(1,1) PRIMARY KEY,
    roles VARCHAR(50)
);
GO

CREATE TABLE Admin (
    userID INT PRIMARY KEY,
    fname VARCHAR(50),
    lname VARCHAR(50),
    email VARCHAR(50) UNIQUE,
    passwords VARCHAR(50),
    FOREIGN KEY (userID) REFERENCES Users(userID)
);
GO

CREATE TABLE Passengers (
    userID INT PRIMARY KEY,
    fname VARCHAR(50),
    lname VARCHAR(50),
    email VARCHAR(50) UNIQUE,
    passwords VARCHAR(50),
    passportNo VARCHAR(20) UNIQUE,
    contactNo VARCHAR(20),
    creditCard VARCHAR(32),
    FOREIGN KEY (userID) REFERENCES Users(userID)
);
GO

CREATE TABLE Crew (
    crewID INT PRIMARY KEY,
    fname VARCHAR(50),
    lname VARCHAR(50),
    designation VARCHAR(50),
    contactNo VARCHAR(20)
);
GO

CREATE TABLE Aircraft (
    aircraftID INT PRIMARY KEY,
    model VARCHAR(50),
    capacity INT,
    regNo VARCHAR(20),
    manufacturer VARCHAR(50),
    yearOfManufacture INT
);
GO

CREATE TABLE Schedule (
    scheduleID INT IDENTITY(1,1) PRIMARY KEY,
    adminID INT,
    aircraftID INT,
    dates DATE,
    gate VARCHAR(50),
    availableSeats INT,
    FOREIGN KEY (adminID) REFERENCES Admin(userID),
    FOREIGN KEY (aircraftID) REFERENCES Aircraft(aircraftID)
);
GO

CREATE TABLE Flights (
    flightID INT PRIMARY KEY,
    departureAirport VARCHAR(50),
    arrivalAirport VARCHAR(50),
    departureTime TIME,
    arrivalTime TIME,
    scheduleID INT,
    status VARCHAR(50),
    FOREIGN KEY (scheduleID) REFERENCES Schedule(scheduleID)
);
GO

CREATE TABLE CrewFlightManagement (
    assignmentID INT IDENTITY(1,1) PRIMARY KEY,
    crewID INT,
    flightID INT,
    FOREIGN KEY (crewID) REFERENCES Crew(crewID),
    FOREIGN KEY (flightID) REFERENCES Flights(flightID)
);
GO


CREATE TABLE Tickets (
    ticketID INT IDENTITY(1,1) PRIMARY KEY,
    passengerID INT,
    flightID INT,
    seatNo VARCHAR(20),
    dates DATE,
    FOREIGN KEY (passengerID) REFERENCES Passengers(userID),
    FOREIGN KEY (flightID) REFERENCES Flights(flightID)
);
GO

CREATE TABLE Payment (
    paymentID INT IDENTITY(1,1) PRIMARY KEY,
    ticketID INT,
    amount INT,
    method VARCHAR(20),
    dates DATE,
    status VARCHAR(20),
    FOREIGN KEY (ticketID) REFERENCES Tickets(ticketID)
);
GO

-- ========== INDEXES ========== 
CREATE INDEX idx_roles ON Users(roles);
CREATE INDEX idx_admin_name ON Admin(lname, fname);
CREATE INDEX idx_passportNo ON Passengers(passportNo);
CREATE INDEX idx_designation ON Crew(designation);
CREATE INDEX idx_regNo ON Aircraft(regNo);
CREATE INDEX idx_dates ON Schedule(dates);
CREATE INDEX idx_scheduleID ON Flights(scheduleID);
CREATE INDEX idx_flightID ON CrewFlightManagement(flightID);
CREATE INDEX idx_passengerID ON Tickets(passengerID);
CREATE INDEX idx_ticketID ON Payment(ticketID);
GO

-- ========== STORED PROCEDURES ========== 
CREATE PROCEDURE AddUser (
    @roles VARCHAR(50)
)
AS
BEGIN
    INSERT INTO Users (roles) VALUES (@roles);
    SELECT SCOPE_IDENTITY() AS newUserID;
END;
GO

CREATE PROCEDURE AddAdmin (
    @fname VARCHAR(50),
    @lname VARCHAR(50),
    @email VARCHAR(50),
    @passwords VARCHAR(50)
)
AS
BEGIN
    DECLARE @newUserID INT;
    INSERT INTO Users (roles) VALUES ('admin');
    SET @newUserID = SCOPE_IDENTITY();
    INSERT INTO Admin (userID, fname, lname, email, passwords)
    VALUES (@newUserID, @fname, @lname, @email, @passwords);
    SELECT @newUserID AS userID;
END;
GO

CREATE PROCEDURE AddPassenger (
    @fname VARCHAR(50),
    @lname VARCHAR(50),
    @email VARCHAR(50),
    @passwords VARCHAR(50),
    @passportNo VARCHAR(20),
    @contactNo VARCHAR(20)
)
AS
BEGIN
    DECLARE @newUserID INT;
    INSERT INTO Users (roles) VALUES ('passenger');
    SET @newUserID = SCOPE_IDENTITY();
    INSERT INTO Passengers (userID, fname, lname, email, passwords, passportNo, contactNo)
    VALUES (@newUserID, @fname, @lname, @email, @passwords, @passportNo, @contactNo);
    SELECT @newUserID AS userID;
END;
GO

CREATE PROCEDURE AddCrew (
    @fname VARCHAR(50),
    @lname VARCHAR(50),
    @designation VARCHAR(50),
    @contactNo VARCHAR(20)
)
AS
BEGIN
    INSERT INTO Crew (fname, lname, designation, contactNo)
    VALUES (@fname, @lname, @designation, @contactNo);
    SELECT SCOPE_IDENTITY() AS newCrewID;
END;
GO

CREATE PROCEDURE AddAircraft (
    @aircraftID INT,
    @model VARCHAR(50),
    @capacity INT,
    @regNo VARCHAR(20),
    @manufacturer VARCHAR(50),
    @yearOfManufacture INT
)
AS
BEGIN
    INSERT INTO Aircraft (aircraftID, model, capacity, regNo, manufacturer, yearOfManufacture)
    VALUES (@aircraftID, @model, @capacity, @regNo, @manufacturer, @yearOfManufacture);
END;
GO

CREATE PROCEDURE AddSchedule (
    @adminID INT,
    @aircraftID INT,
    @dates DATE,
    @gate VARCHAR(50)
)
AS
BEGIN
    INSERT INTO Schedule (adminID, aircraftID, dates, gate)
    VALUES (@adminID, @aircraftID, @dates, @gate);
    SELECT SCOPE_IDENTITY() AS newScheduleID;
END;
GO

CREATE PROCEDURE AddFlight (
    @flightID INT,
    @departureAirport VARCHAR(50),
    @arrivalAirport VARCHAR(50),
    @departureTime TIME,
    @arrivalTime TIME,
    @scheduleID INT,
    @status VARCHAR(50)
)
AS
BEGIN
    INSERT INTO Flights (flightID, departureAirport, arrivalAirport, departureTime, arrivalTime, scheduleID, status)
    VALUES (@flightID, @departureAirport, @arrivalAirport, @departureTime, @arrivalTime, @scheduleID, @status);
END;
GO

CREATE PROCEDURE AddCrewFlightAssignment (
    @crewID INT,
    @flightID INT
)
AS
BEGIN
    INSERT INTO CrewFlightManagement (crewID, flightID)
    VALUES (@crewID, @flightID);
    
    SELECT SCOPE_IDENTITY() AS assignmentID;
END;
GO

CREATE PROCEDURE AddTicket (
    @passengerID INT,
    @flightID INT,
    @seatNo VARCHAR(20),
    @dates DATE
)
AS
BEGIN
    INSERT INTO Tickets (passengerID, flightID, seatNo, dates)
    VALUES (@passengerID, @flightID, @seatNo, @dates);
    SELECT SCOPE_IDENTITY() AS ticketID;
END;
GO

CREATE PROCEDURE AddPayment (
    @ticketID INT,
    @amount INT,
    @method VARCHAR(20),
    @dates DATE,
    @status VARCHAR(20)
)
AS
BEGIN
    INSERT INTO Payment (ticketID, amount, method, dates, status)
    VALUES (@ticketID, @amount, @method, @dates, @status);
    SELECT SCOPE_IDENTITY() AS paymentID;
END;
GO

CREATE OR ALTER PROCEDURE DeletePayment
    @ticketID INT
AS
BEGIN
    DELETE FROM Payment WHERE ticketID = @ticketID;
END;
GO

CREATE OR ALTER PROCEDURE DeleteTicket
    @ticketID INT
AS
BEGIN
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- First delete the payment
        EXEC DeletePayment @ticketID;
        
        -- Then delete the ticket
        DELETE FROM Tickets WHERE ticketID = @ticketID;
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

CREATE OR ALTER PROCEDURE GetTotalIncome
AS
BEGIN
    SELECT ISNULL(SUM(p.amount), 0) AS totalIncome
    FROM Payment p
    JOIN Tickets t ON p.ticketID = t.ticketID
    JOIN Flights f ON t.flightID = f.flightID
    WHERE f.status IN ('Scheduled', 'Completed', 'Booked')
      AND p.status = 'Completed';
END

-- ========== VIEWS ========== 
CREATE VIEW ViewPassengerDetails AS
SELECT P.userID, P.fname, P.lname, P.email, P.passportNo, P.contactNo, U.roles
FROM Passengers P
JOIN Users U ON P.userID = U.userID;
GO

CREATE VIEW ViewFlightSchedule AS
SELECT F.flightID, F.departureAirport, F.arrivalAirport, F.departureTime, F.arrivalTime,
       S.dates AS scheduleDate, A.model AS aircraftModel, A.regNo
FROM Flights F
JOIN Schedule S ON F.scheduleID = S.scheduleID
JOIN Aircraft A ON S.aircraftID = A.aircraftID;
GO

-- ========== TRIGGERS ========== 
CREATE TRIGGER trg_UpdateFlightStatusAfterBooking
ON Tickets
AFTER INSERT
AS
BEGIN
    UPDATE Flights
    SET status = 'Booked'
    WHERE flightID IN (SELECT flightID FROM inserted);
END;
GO

DROP TRIGGER IF EXISTS trg_PreventOverbooking;
GO

CREATE TRIGGER trg_PreventOverbooking
ON Tickets
AFTER INSERT
AS
BEGIN
    IF EXISTS (
        SELECT t.flightID
        FROM Tickets t
        JOIN inserted i ON t.flightID = i.flightID
        GROUP BY t.flightID
        HAVING COUNT(*) > (
            SELECT a.capacity
            FROM Flights f
            JOIN Schedule s ON f.scheduleID = s.scheduleID
            JOIN Aircraft a ON s.aircraftID = a.aircraftID
            WHERE f.flightID = t.flightID
        )
    )
    BEGIN
        RAISERROR('Cannot insert ticket: Flight is fully booked.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

CREATE OR ALTER TRIGGER trg_RestoreScheduleSeatsAfterTicketDelete
ON Tickets
AFTER DELETE
AS
BEGIN
    WITH ScheduleRestore AS (
        SELECT s.scheduleID, COUNT(*) AS restoreCount
        FROM deleted d
        JOIN Flights f ON d.flightID = f.flightID
        JOIN Schedule s ON f.scheduleID = s.scheduleID
        GROUP BY s.scheduleID
    )
    UPDATE s
    SET s.availableSeats = s.availableSeats + sr.restoreCount
    FROM Schedule s
    JOIN ScheduleRestore sr ON s.scheduleID = sr.scheduleID;
END;
GO

CREATE OR ALTER TRIGGER trg_DecreaseScheduleSeatsAfterBooking
ON Tickets
AFTER INSERT
AS
BEGIN
    UPDATE s
    SET s.availableSeats = s.availableSeats - 1
    FROM Schedule s
    JOIN Flights f ON s.scheduleID = f.scheduleID
    JOIN inserted i ON f.flightID = i.flightID
    WHERE s.availableSeats > 0;

    IF EXISTS (
        SELECT 1
        FROM Schedule s
        JOIN Flights f ON s.scheduleID = f.scheduleID
        JOIN inserted i ON f.flightID = i.flightID
        WHERE s.availableSeats < 0
    )
    BEGIN
        RAISERROR ('No available seats for this flight.', 16, 1);
        ROLLBACK TRANSACTION;
    END
END;
GO

-- Initialize availableSeats for existing schedules to the aircraft's capacity
CREATE TRIGGER trg_SetAvailableSeatsOnInsert
ON Schedule
AFTER INSERT
AS
BEGIN
    UPDATE s
    SET s.availableSeats = a.capacity
    FROM Schedule s
    JOIN inserted i ON s.scheduleID = i.scheduleID
    JOIN Aircraft a ON i.aircraftID = a.aircraftID
    WHERE s.scheduleID = i.scheduleID;
END


ALTER TABLE Schedule ADD availableSeats INT;

ALTER TABLE Passengers
ADD creditCard VARCHAR(32);

-- ========== DELETE DATA FROM ALL TABLES IN ORDER ==========
DELETE FROM Payment;
DELETE FROM Tickets;
DELETE FROM CrewFlightManagement;
DELETE FROM Flights;
DELETE FROM Schedule;
DELETE FROM Aircraft;
DELETE FROM Crew;
DELETE FROM Passengers;
DELETE FROM Admin;
DELETE FROM Users;

INSERT INTO Aircraft (aircraftID, model, capacity, regNo, manufacturer, yearOfManufacture)
VALUES
(1, 'Boeing 737', 180, 'REG737A', 'Boeing', 2015),
(2, 'Airbus A320', 150, 'REG320B', 'Airbus', 2017),
(3, 'Boeing 777', 396, 'REG777C', 'Boeing', 2012),
(4, 'Embraer E190', 100, 'REG190D', 'Embraer', 2019),
(5, 'Airbus A350', 325, 'REG350E', 'Airbus', 2020);

select * from CrewFlightManagement

select * from Tickets
select * from Payment

select * from Flights
select * from Schedule

delete

select * from Passengers
select * from Admin