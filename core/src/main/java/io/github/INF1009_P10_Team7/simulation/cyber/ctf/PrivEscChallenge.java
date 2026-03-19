package io.github.INF1009_P10_Team7.simulation.cyber.ctf;

/**
 * CTF Challenge 4  -  Privilege Escalation (Level 2 exclusive).
 *
 * Flow:
 *   sudo -l              → see NOPASSWD: /usr/bin/find
 *   sudo find / -name "passwd" -exec /bin/bash \;  OR
 *   sudo find /etc -exec cat {} \; (hint approach)
 *   whoami               → root (after sudo exploit)
 *   cat /root/flag.txt   → FLAG{r00t_4ccess_gr4nted}
 *
 * Strategy Pattern: implements ICTFChallenge.
 */
public class PrivEscChallenge implements ICTFChallenge {

    private enum Stage { CONNECTED, SUDO_FOUND, ROOT, DONE }
    private Stage stage = Stage.CONNECTED;
    private boolean solved = false;

    @Override public String getTitle()      { return "TERMINAL-04 // PRIVILEGE ESCALATION"; }
    @Override public String getTargetInfo() { return "target: 10.0.0.50 (low-priv shell)"; }
    @Override public String getPrompt() {
        if (stage == Stage.ROOT || stage == Stage.DONE) return "root@target:/# ";
        return "www-data@target:/var/www$ ";
    }

    @Override
    public TerminalLine[] getWelcomeLines() {
        return new TerminalLine[]{
            TerminalLine.warn("OBJECTIVE: Escalate from www-data to root and grab the flag."),
            TerminalLine.dim ("----------------------------------------"),
            TerminalLine.out ("You have a low-privilege web-shell on 10.0.0.50."),
            TerminalLine.out ("Goal: become root and read /root/flag.txt"),
            TerminalLine.dim ("Hint: check what you can run as sudo without a password."),
        };
    }

    @Override
    public TerminalLine[] processInput(String raw) {
        String cmd = raw.trim();
        String lo  = cmd.toLowerCase();

        if (lo.equals("id") || lo.equals("whoami")) {
            if (stage == Stage.ROOT || stage == Stage.DONE)
                return new TerminalLine[]{ TerminalLine.ok("uid=0(root) gid=0(root) groups=0(root)") };
            return new TerminalLine[]{ TerminalLine.out("uid=33(www-data) gid=33(www-data) groups=33(www-data)") };
        }

        if (lo.startsWith("sudo -l")) {
            if (stage == Stage.CONNECTED) stage = Stage.SUDO_FOUND;
            return sudoList();
        }

        if (lo.startsWith("uname")) {
            return new TerminalLine[]{ TerminalLine.out("Linux target 5.15.0-91-generic #101-Ubuntu SMP x86_64 GNU/Linux") };
        }

        if (lo.startsWith("ls")) {
            if (stage == Stage.ROOT || stage == Stage.DONE)
                return new TerminalLine[]{ TerminalLine.out("flag.txt  .bashrc  .ssh/") };
            return new TerminalLine[]{ TerminalLine.out("index.php  config.php  uploads/") };
        }

        if (lo.startsWith("cat /etc/passwd") || lo.startsWith("cat /etc/shadow")) {
            if (lo.contains("shadow") && stage != Stage.ROOT && stage != Stage.DONE)
                return new TerminalLine[]{ TerminalLine.err("cat: /etc/shadow: Permission denied") };
            return new TerminalLine[]{
                TerminalLine.out("root:x:0:0:root:/root:/bin/bash"),
                TerminalLine.out("www-data:x:33:33:www-data:/var/www:/usr/sbin/nologin"),
                TerminalLine.dim("... (truncated)"),
            };
        }

        // The exploit: sudo find ... -exec /bin/bash
        if (stage == Stage.SUDO_FOUND || stage == Stage.ROOT) {
            if (lo.startsWith("sudo find") && (lo.contains("-exec") || lo.contains("bash") || lo.contains("sh"))) {
                stage = Stage.ROOT;
                return rootShell();
            }
            // simpler shortcut: sudo /usr/bin/find . -exec /bin/sh \;
            if (lo.contains("sudo") && lo.contains("find")) {
                stage = Stage.ROOT;
                return rootShell();
            }
        }

        if (stage == Stage.ROOT || stage == Stage.DONE) {
            if (lo.startsWith("cat /root/flag.txt") || lo.startsWith("cat flag.txt")) {
                return captureFlag();
            }
            if (lo.startsWith("cd /root") || lo.startsWith("cd ~")) {
                return new TerminalLine[]{ TerminalLine.dim("") };
            }
            if (lo.startsWith("pwd"))
                return new TerminalLine[]{ TerminalLine.out("/root") };
        }

        if (lo.equals("help") || lo.equals("?")) return helpLines();
        if (lo.equals("exit") || lo.equals("quit"))
            return new TerminalLine[]{ TerminalLine.dim("Session closed.") };

        String c = cmd.split("\\s+")[0];
        return new TerminalLine[]{ TerminalLine.err("bash: " + c + ": command not found") };
    }

    @Override public boolean isSolved() { return solved; }
    @Override public void reset() { stage = Stage.CONNECTED; solved = false; }

    // ---- Sub-responses ----

    private TerminalLine[] sudoList() {
        return new TerminalLine[]{
            TerminalLine.out ("Matching Defaults entries for www-data on target:"),
            TerminalLine.out ("    env_reset, mail_badpass"),
            TerminalLine.blank(),
            TerminalLine.warn("User www-data may run the following commands on target:"),
            TerminalLine.ok  ("    (ALL) NOPASSWD: /usr/bin/find"),
            TerminalLine.blank(),
            TerminalLine.dim ("Hint: GTFOBins  -  sudo find / -exec /bin/bash \\;"),
        };
    }

    private TerminalLine[] rootShell() {
        return new TerminalLine[]{
            TerminalLine.ok  (""),
            TerminalLine.warn("########################################"),
            TerminalLine.ok  ("  ROOT SHELL OBTAINED"),
            TerminalLine.warn("########################################"),
            TerminalLine.ok  ("uid=0(root) gid=0(root) groups=0(root)"),
            TerminalLine.dim ("Now read: cat /root/flag.txt"),
        };
    }

    private TerminalLine[] captureFlag() {
        solved = true;
        return new TerminalLine[]{
            TerminalLine.blank(),
            TerminalLine.ok  ("||||||||||||||||||||||||||||||||||||||||||"),
            TerminalLine.ok  ("  FLAG{r00t_4ccess_gr4nted_sudo_find_ftw}"),
            TerminalLine.ok  ("||||||||||||||||||||||||||||||||||||||||||"),
            TerminalLine.blank(),
            TerminalLine.warn(">>> FULL SYSTEM COMPROMISE  -  ACCESS KEY EXTRACTED <<<"),
        };
    }

    private TerminalLine[] helpLines() {
        return new TerminalLine[]{
            TerminalLine.info("Shell commands:"),
            TerminalLine.out ("  id / whoami          -  current user"),
            TerminalLine.out ("  sudo -l              -  list sudo privileges"),
            TerminalLine.out ("  sudo find [args]     -  run find as root (GTFOBins)"),
            TerminalLine.out ("  cat <file>           -  read file"),
            TerminalLine.out ("  ls                   -  list files"),
            TerminalLine.out ("  uname -a             -  kernel info"),
        };
    }
}
