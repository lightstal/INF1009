package io.github.INF1009_P10_Team7.simulation.cyber.ctf;

/**
 * CTF Challenge 3  -  SQL Injection.
 *
 * Flow the player must find:
 *   sqlmap -u "http://10.10.10.50/login?id=1" -D appdb -T users --dump
 *   login admin S3cr3t!Flag
 *   cat /var/www/flag.txt  →  FLAG{sqli_d4t4b4s3_pwn3d}
 */
/**
 * SqlInjectionChallenge — CTF terminal challenge simulating a web SQL injection attack.
 *
 * <p>The player must dump credentials from the database, log in as admin, then read
 * {@code /var/www/flag.txt}.</p>
 *
 * <p>Implements {@link ICTFChallenge} so it is usable as a
 * {@link io.github.INF1009_P10_Team7.simulation.cyber.minigame.TerminalMiniGame}
 * (OCP, LSP).</p>
 *
 * <p>Progression stages:</p>
 * <ol>
 *   <li>{@code INITIAL}   — default start</li>
 *   <li>{@code DUMPED}    — after {@code sqlmap ... --dump}</li>
 *   <li>{@code LOGGED_IN} — after {@code login admin S3cr3t!Flag}</li>
 * </ol>
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
            TerminalLine.warn("OBJECTIVE: Dump the database and capture the flag."),
            TerminalLine.out ("Target: http://10.10.10.50  |  Flag: /var/www/flag.txt"),
            TerminalLine.dim ("Hint: sqlmap -u \"http://10.10.10.50/login?id=1\" -D appdb -T users --dump"),
        };
    }

    @Override
    public TerminalLine[] processInput(String raw) {
        String cmd = raw.trim();
        String lo  = cmd.toLowerCase();

        if (lo.startsWith("sqlmap")) return handleSqlmap(lo);
        if (lo.startsWith("login")) return handleLogin(cmd);

        if (lo.startsWith("cat /var/www/flag") || lo.startsWith("cat flag")) {
            if (stage == Stage.LOGGED_IN) return catFlag();
            return new TerminalLine[]{ TerminalLine.err("Permission denied. You must authenticate first.") };
        }

        if (lo.equals("help") || lo.equals("?")) return help();
        if (lo.equals("clear")) return new TerminalLine[]{};

        String c = cmd.split("\\s+")[0];
        return new TerminalLine[]{ TerminalLine.err("bash: " + c + ": command not found") };
    }

    @Override public boolean isSolved() { return solved; }
    @Override public void reset() { stage = Stage.INITIAL; solved = false; }

    // ── sqlmap handler ────────────────────────────────────────────────

    private TerminalLine[] handleSqlmap(String lo) {
        if (!lo.contains("10.10.10.50")) {
            return new TerminalLine[]{ TerminalLine.err("sqlmap: No target specified.") };
        }
        if (lo.contains("--dump") && lo.contains("appdb") && lo.contains("users")) {
            stage = Stage.DUMPED;
            return sqlmapDump();
        }
        return new TerminalLine[]{
            TerminalLine.warn("Specify the database and table to dump."),
            TerminalLine.dim ("sqlmap -u \"http://10.10.10.50/login?id=1\" -D appdb -T users --dump"),
        };
    }

    private TerminalLine[] sqlmapDump() {
        return new TerminalLine[]{
            TerminalLine.dim ("[INFO] fetching columns for table 'users'"),
            TerminalLine.dim ("[INFO] fetching entries for table 'users'"),
            TerminalLine.blank(),
            TerminalLine.out ("Table: users"),
            TerminalLine.dim ("+----+----------+---------------------+-------------+"),
            TerminalLine.dim ("| id | username |       email         |  password   |"),
            TerminalLine.dim ("+----+----------+---------------------+-------------+"),
            TerminalLine.out ("| 1  | admin    | admin@company.local | S3cr3t!Flag |"),
            TerminalLine.out ("| 2  | bob      | bob@company.local   | bob123      |"),
            TerminalLine.out ("| 3  | alice    | alice@company.local | al1c3pw     |"),
            TerminalLine.dim ("+----+----------+---------------------+-------------+"),
            TerminalLine.blank(),
            TerminalLine.warn("Credentials dumped! Try: login admin S3cr3t!Flag"),
        };
    }

    // ── login handler ────────────────────────────────────────────────

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

    // ── flag ─────────────────────────────────────────────────────────

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

    // ── help ─────────────────────────────────────────────────────────

    private TerminalLine[] help() {
        return new TerminalLine[]{
            TerminalLine.info("Steps:"),
            TerminalLine.out ("  sqlmap -u \"http://10.10.10.50/login?id=1\" -D appdb -T users --dump"),
            TerminalLine.out ("  login admin S3cr3t!Flag"),
            TerminalLine.out ("  cat /var/www/flag.txt"),
        };
    }

    private String extractArg(String cmd) {
        String[] p = cmd.split("\\s+", 2);
        return p.length > 1 ? p[1].trim() : "";
    }
}
