package io.github.INF1009_P10_Team7.cyber.level;

import io.github.INF1009_P10_Team7.cyber.ctf.NmapReconChallenge;
import io.github.INF1009_P10_Team7.cyber.ctf.TerminalEmulator;
import io.github.INF1009_P10_Team7.cyber.components.drone.DroneAI;
import io.github.INF1009_P10_Team7.cyber.minigame.*;

/**
 * Level 1 – RECON LAB configuration (Strategy Pattern).
 */
public class Level1Config implements LevelConfig {

    @Override public int    getLevelNumber()   { return 1; }
    @Override public String getLevelName()     { return "LEVEL 1  -  RECON LAB"; }
    @Override public String getIntroSubtitle() {
        return "No drones yet. Learn the terminals, checkpoints, and signal ping.";
    }

    @Override public String getMapFile()         { return "maps/Level1.tmx"; }
    @Override public String getCollisionLayer()  { return "collision"; }
    @Override public String getWallLayer()       { return "Walls"; }
    @Override public String getDoorLayer()       { return "doors"; }

    @Override
    public IMiniGame[] createChallenges(TerminalEmulator terminal) {

        // ── PortMatchGame — Level 1 (easy, well-known ports) ─────────────────
        // ports[i] maps to services[correct[i]]
        //   22   -> SSH   (index 2 = C)
        //   80   -> HTTP  (index 4 = E)
        //   443  -> HTTPS (index 3 = D)
        //   3306 -> MySQL (index 0 = A)
        //   21   -> FTP   (index 1 = B)
        String[] pmPorts    = { "22",    "80",    "443",   "3306",  "21"   };
        String[] pmServices = { "MySQL", "FTP",   "SSH",   "HTTPS", "HTTP" };
        int[]    pmCorrect  = {  2,       4,       3,       0,       1     };

        // ── LogAnalysisGame — Level 1 (plaintext, no cipher) ─────────────────
        // The override code appears in plain text inside the email.
        // Player just reads the document and copies it.
        // To change: edit lgAnswer and the matching line inside lgDocument.
        String lgAnswer    = "override_7734_alpha";
        String lgHighlight = "OVERRIDE_7734_ALPHA";  // line containing this gets highlighted green
        String lgHint      = "Read the document above. Find the OVERRIDE CODE and type it below, then press ENTER.";
        String lgWrong     = "INCORRECT - KEEP READING THE DOCUMENT";
        String[] lgDocument = {
            "=========================================================",
            "  INTERCEPTED TRANSMISSION - FILE: corp_mail_dump.txt",
            "  Source: Internal SMTP relay @ 192.168.10.11",
            "=========================================================",
            "",
            "FROM:    audit-bot@corpserver.internal",
            "TO:      it-ops@corpserver.internal",
            "DATE:    Mon Nov 11 09:14:02 2024",
            "SUBJECT: [AUTOMATED] Credential Rotation Required - ACTION NEEDED",
            "",
            "-----BEGIN MESSAGE-----",
            "",
            "Dear IT Operations Team,",
            "",
            "Routine security audit has flagged THREE expired service tokens.",
            "Per policy CP-12, these must be rotated within 48 hours.",
            "",
            "  [1] backup-svc      token expired:  2024-11-08",
            "  [2] ci-pipeline     token expired:  2024-11-07",
            "  [3] monitoring-agent  last rotated: 347 days ago",
            "",
            "Please log in to the internal portal and rotate the above.",
            "Reference Internal Knowledge Base Article #7734 for the",
            "step-by-step procedure.",
            "",
            "-----END MESSAGE-----",
            "",
            "=========================================================",
            "",
            "FROM:    r.chen@corpserver.internal (Robert Chen, Head of IT)",
            "TO:      it-ops@corpserver.internal",
            "DATE:    Mon Nov 11 11:32:17 2024",
            "SUBJECT: RE: [AUTOMATED] Credential Rotation Required",
            "",
            "-----BEGIN MESSAGE-----",
            "",
            "Team,",
            "",
            "Quick update - I've pre-rotated the backup-svc and ci-pipeline",
            "tokens already. The monitoring-agent key is more sensitive,",
            "so I'm handling that one separately.",
            "",
            "For anyone who needs emergency access during the rotation window,",
            "the temporary OVERRIDE code is active until Thursday 23:59.",
            "",
            "     >>> OVERRIDE CODE: OVERRIDE_7734_ALPHA <<<",
            "",
            "DO NOT share this outside the ops team. Memorise it,",
            "then DELETE this email.",
            "",
            "(Yes I know I should not put this in email. I was in a rush.)",
            "   -- R. Chen",
            "",
            "-----END MESSAGE-----",
            "",
            "=========================================================",
            "",
            "FROM:    j.santos@corpserver.internal (Joao Santos, Sysadmin)",
            "TO:      r.chen@corpserver.internal",
            "DATE:    Mon Nov 11 14:05:44 2024",
            "SUBJECT: RE: RE: [AUTOMATED] Credential Rotation Required",
            "",
            "-----BEGIN MESSAGE-----",
            "",
            "Robert,",
            "",
            "Got it. I'll rotate monitoring-agent this evening after the",
            "backup window closes (~22:00). Will confirm once done.",
            "",
            "Also - you might want to send that override code via Signal",
            "next time, not email. The SMTP relay logs EVERYTHING.",
            "",
            "   -- J. Santos",
            "",
            "-----END MESSAGE-----",
            "",
            "=========================================================",
            "  END OF INTERCEPTED TRANSMISSION",
            "  Total messages: 3   |   Attachments: 0",
            "=========================================================",
        };

        return new IMiniGame[]{
            new BinaryDecodeGame("FLAGS"),   // change word here to update the binary puzzle
            new CaesarCipherGame(),
            new PortMatchGame(pmPorts, pmServices, pmCorrect),
            new LogAnalysisGame(lgDocument, lgAnswer, lgHighlight, lgHint, lgWrong),
            new TerminalMiniGame(new NmapReconChallenge(), terminal)
        };
    }

    @Override public int      getKeysRequired()  { return 5; }
    @Override public float    getTimeLimit()     { return 360f; }
    @Override public DroneAI[] createDrones()    { return new DroneAI[]{}; }
    @Override public int[]    getPlayerStartTile() { return new int[]{ 19, 12 }; }

    @Override public int[][] getCameraPositions() {
        return new int[][]{ {19, 8, 270} };
    }
    @Override public int[][] getLightPositions() {
        return new int[][]{ {19, 5}, {19, 12}, {19, 18} };
    }
}
