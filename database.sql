USE master;
GO

IF DB_ID('PigTracker') IS NOT NULL
BEGIN
    ALTER DATABASE PigTracker SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE PigTracker;
END
GO

CREATE DATABASE PigTracker;
GO

USE PigTracker;
GO

DROP TABLE IF EXISTS Users;
GO

CREATE TABLE Users (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    username    NVARCHAR(50)  NOT NULL UNIQUE,
    password    NVARCHAR(255) NOT NULL,
    permission  NVARCHAR(10)  NOT NULL
        CONSTRAINT DF_Users_permission DEFAULT 'DEFAULT'
        CONSTRAINT CK_Users_permission CHECK (permission IN ('ADMIN', 'DEFAULT'))
);
GO

DROP TABLE IF EXISTS Groups;
GO

CREATE TABLE Groups (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    creation_status NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Groups_creation_status DEFAULT 'IN_PROGRESS'
        CONSTRAINT CK_Groups_creation_status CHECK (creation_status IN ('IN_PROGRESS', 'COMPLETE'))
);
GO

DROP TABLE IF EXISTS Reports;
GO

CREATE TABLE Reports (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    group_id     INT          NOT NULL
        CONSTRAINT FK_Reports_group_id FOREIGN KEY REFERENCES Groups(id),
    import_start DATETIME2(0) NOT NULL,
    import_end   DATETIME2(0) NOT NULL,
    row_count    INT          NOT NULL,
    pig_count    INT          NOT NULL,
    status       NVARCHAR(20) NOT NULL
        CONSTRAINT DF_Reports_status DEFAULT 'IN_PROGRESS'
        CONSTRAINT CK_Reports_status CHECK (status IN ('IN_PROGRESS', 'COMPLETE')),
    created_by   INT NOT NULL
        CONSTRAINT FK_Reports_created_by FOREIGN KEY REFERENCES Users(id),
    created_at   DATETIME2(0) NOT NULL
        CONSTRAINT DF_Reports_created_at DEFAULT SYSUTCDATETIME()
);
GO

DROP TABLE IF EXISTS Animals;
GO

CREATE TABLE Animals (
    id              INT IDENTITY(1,1) PRIMARY KEY,  
    animal_number   INT           NOT NULL,
    responder       VARCHAR(20)   NOT NULL,
    group_id        INT           NOT NULL
        CONSTRAINT FK_Animals_group_id FOREIGN KEY REFERENCES Groups(id),
    location        INT           NOT NULL,
    status          NVARCHAR(10)  NOT NULL
        CONSTRAINT DF_Animals_status DEFAULT 'ACTIVE'
        CONSTRAINT CK_Animals_status CHECK (status IN ('ACTIVE', 'STOPPED')),
    stopped_reason  NVARCHAR(255) NULL,
    stopped_at      DATETIME2(0)  NULL,
    fcr             DECIMAL(6,2)  NULL,
    start_weight_kg DECIMAL(7,2)  NULL,
    total_feed_kg   DECIMAL(7,2)  NULL,
    weight_gain_kg  DECIMAL(7,2)  NULL,
    latest_weight_kg   DECIMAL(7,2)  NULL,
    completed_days  INT           NULL,
    start_day       DATE          NULL,
    created_at      DATETIME2(0)  NOT NULL
        CONSTRAINT DF_Animals_created_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT UQ_Animals_responder UNIQUE (responder),
        CONSTRAINT UQ_Animals_animal_number UNIQUE (animal_number)
);
GO

CREATE INDEX IX_Animals_location ON Animals (location);
CREATE INDEX IX_Animals_status   ON Animals (status);
GO

DROP TABLE IF EXISTS Visits;
GO

CREATE TABLE Visits (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    animal_number INT          NOT NULL 
        CONSTRAINT FK_Visits_AnimalNumber FOREIGN KEY REFERENCES Animals(animal_number),
    report_id     INT          NOT NULL 
        CONSTRAINT FK_Visits_ReportId FOREIGN KEY REFERENCES Reports(id),
    responder     VARCHAR(20)  NOT NULL
        CONSTRAINT FK_Visits_Responder FOREIGN KEY REFERENCES Animals(responder),
    location      INT          NOT NULL,
    visit_time    DATETIME2(0) NOT NULL,
    duration_sec  INT          NOT NULL,
    weight_g      INT          NULL,
    feed_intake_g INT          NOT NULL
);
GO

CREATE INDEX IX_Visits_responder  ON Visits (responder);
CREATE INDEX IX_Visits_visit_time ON Visits (visit_time);
GO