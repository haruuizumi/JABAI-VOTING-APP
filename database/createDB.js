const sqlite3 = require("sqlite3").verbose();

const db = new sqlite3.Database("voting.db", (err) => {
    if (err) console.error(err);
    else console.log("Database created: voting.db");
});

// Create tables
db.serialize(() => {

    // Candidates table
    db.run(`
        CREATE TABLE IF NOT EXISTS candidates (
            name TEXT UNIQUE,
            position TEXT,
            course TEXT,
            year TEXT,
            section TEXT,
            description TEXT
        )
    `);

    // Voters table
    db.run(`
        CREATE TABLE IF NOT EXISTS voters (
            studentID TEXT PRIMARY KEY,
            name TEXT,
            email TEXT,
            course TEXT,
            year TEXT,
            section TEXT,
            hasVoted INTEGER DEFAULT 0,
            voteTimestamp TEXT
        )
    `);

    // Votes table
    db.run(`
        CREATE TABLE IF NOT EXISTS votes (
            timestamp TEXT,
            candidate TEXT,
            position TEXT,
            course TEXT,
            year TEXT,
            section TEXT
        )
    `);

    console.log("All tables created successfully!");
});

db.close();
