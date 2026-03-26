package io.github.INF1009_P10_Team7.cyber.level;

import io.github.INF1009_P10_Team7.cyber.ctf.SqlInjectionChallenge;
import io.github.INF1009_P10_Team7.cyber.ctf.TerminalEmulator;
import io.github.INF1009_P10_Team7.cyber.components.drone.DroneAI;
import io.github.INF1009_P10_Team7.cyber.minigame.*;

/**
 * Level 2 – NETWORK HUB configuration (Strategy Pattern).
 */
public class Level2Config implements LevelConfig {

    @Override public int    getLevelNumber()   { return 2; }
    @Override public String getLevelName()     { return "LEVEL 2  -  NETWORK HUB"; }
    @Override public String getIntroSubtitle() {
        return "Break line of sight at corners and do not rush into the center lane.";
    }

    @Override public String getMapFile()        { return "maps/Level2.tmx"; }
    @Override public String getCollisionLayer() { return "collision"; }
    @Override public String getWallLayer()      { return "Walls"; }
    @Override public String getDoorLayer()      { return "door"; }

    @Override
    public IMiniGame[] createChallenges(TerminalEmulator terminal) {

        // ── PortMatchGame — Level 2 (hard, less common protocols) ────────────
        // ports[i] maps to services[correct[i]]
        //   25   -> SMTP       (index 3 = D)
        //   110  -> POP3       (index 4 = E)
        //   143  -> IMAP       (index 1 = B)
        //   3389 -> RDP        (index 2 = C)
        //   5432 -> PostgreSQL (index 0 = A)
        String[] pmPorts    = { "25",         "110",  "143",  "3389", "5432"       };
        String[] pmServices = { "PostgreSQL", "IMAP", "RDP",  "SMTP", "POP3"       };
        int[]    pmCorrect  = {  3,            4,      1,      2,      0            };

        // ── LogAnalysisGame — Level 2 (Atbash cipher) ────────────────────────
        // The override code is encoded with Atbash (A<->Z mirror cipher).
        // Ciphertext in email : ZXXVHH_9182_WVOGZ
        // Plaintext answer    : access_9182_delta
        // Hints woven in:
        //   Email 2 — "mirror encoding", "first letter becomes last, last becomes first"
        //   Email 3 — "Z is A and A is Z"
        // To change: update lgAnswer, lgHighlight, and the matching line in lgDocument.
        String lgAnswer    = "access_9182_delta";
        String lgHighlight = "ZXXVHH_9182_WVOGZ";   // line containing this gets highlighted green
        String lgHint      = "Find the encoded override, decode it using the cipher hinted in the email, then type the plaintext and press ENTER.";
        String lgWrong     = "INCORRECT - CHECK THE CIPHER HINT AND TRY AGAIN";
        String[] lgDocument = {
            "=========================================================",
            "  INTERCEPTED TRANSMISSION - FILE: corp_mail_dump.txt",
            "  Source: Internal SMTP relay @ 192.168.10.11",
            "=========================================================",
            "",
            "FROM:    audit-bot@corpserver.internal",
            "TO:      it-ops@corpserver.internal",
            "DATE:    Tue Nov 12 08:55:01 2024",
            "SUBJECT: [AUTOMATED] Emergency Access Token Expiry - ACTION NEEDED",
            "",
            "-----BEGIN MESSAGE-----",
            "",
            "Dear IT Operations Team,",
            "",
            "An emergency access token for the CORE-INFRA vault has expired",
            "ahead of schedule.  Per policy EP-03, a temporary override code",
            "must be issued and distributed to on-call staff within 2 hours.",
            "",
            "  Asset    : core-infra-vault",
            "  Token ID : TK-9182",
            "  Expired  : 2024-11-12 08:00 UTC",
            "",
            "Please coordinate with the Head of IT to issue and circulate",
            "the override.  Log all access attempts in ticket #9182.",
            "",
            "-----END MESSAGE-----",
            "",
            "=========================================================",
            "",
            "FROM:    r.chen@corpserver.internal (Robert Chen, Head of IT)",
            "TO:      it-ops@corpserver.internal",
            "DATE:    Tue Nov 12 09:21:44 2024",
            "SUBJECT: RE: [AUTOMATED] Emergency Access Token Expiry",
            "",
            "-----BEGIN MESSAGE-----",
            "",
            "Team,",
            "",
            "Override code is ready.  I have encoded it using our legacy",
            "mirror encoding so it is safe to transmit over email.",
            "You know the method - the first letter of the alphabet becomes",
            "the last, and the last becomes the first.  Every letter flips",
            "to its opposite across the alphabet.  Numbers stay as-is.",
            "",
            "     >>> ENCODED OVERRIDE: ZXXVHH_9182_WVOGZ <<<",
            "",
            "Decode it, memorise the result, then delete this message.",
            "The override is valid until 23:59 tonight.",
            "",
            "   -- R. Chen",
            "",
            "-----END MESSAGE-----",
            "",
            "=========================================================",
            "",
            "FROM:    j.santos@corpserver.internal (Joao Santos, Sysadmin)",
            "TO:      r.chen@corpserver.internal",
            "DATE:    Tue Nov 12 09:47:03 2024",
            "SUBJECT: RE: RE: [AUTOMATED] Emergency Access Token Expiry",
            "",
            "-----BEGIN MESSAGE-----",
            "",
            "Robert,",
            "",
            "Got the code, decoding now.  Quick note though - you basically",
            "handed them half the solution by writing 'mirror encoding' in",
            "the same email as the ciphertext.  Anyone who remembers that",
            "Z is A and A is Z can crack that in under a minute.",
            "",
            "Next time send the decode key separately, please.",
            "I'll confirm once I have used the override on the vault.",
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
            new BinaryDecodeGame("CRYPT"),   // change word here to update the binary puzzle
            new PacketSnifferGame(),
            new PortMatchGame(pmPorts, pmServices, pmCorrect),
            new LogAnalysisGame(lgDocument, lgAnswer, lgHighlight, lgHint, lgWrong),
            new TerminalMiniGame(new SqlInjectionChallenge(), terminal)
        };
    }

    @Override public int   getKeysRequired()  { return 5; }
    @Override public float getTimeLimit()     { return 390f; }

    @Override
    public DroneAI[] createDrones() {
        return new DroneAI[]{
            new DroneAI(TileMap.tileCentreX(5), TileMap.tileCentreY(5),
                new float[][]{ {5,5}, {9,8}, {19,11}, {9,13}, {5,17}, {9,13}, {19,11}, {9,8} }),
            new DroneAI(TileMap.tileCentreX(33), TileMap.tileCentreY(17),
                new float[][]{ {33,5}, {28,8}, {19,11}, {28,13}, {33,17}, {28,13}, {19,11}, {28,8} }),
            new DroneAI(TileMap.tileCentreX(7), TileMap.tileCentreY(3),
                new float[][]{ {7,3}, {12,8}, {19,8}, {27,8}, {31,3}, {27,8}, {19,8}, {12,8} })
        };
    }

    @Override public int[] getPlayerStartTile() { return new int[]{ 19, 11 }; }

    @Override public int[][] getCameraPositions() {
        return new int[][]{ {8,8,270}, {27,8,270}, {8,14,90}, {27,14,90},
            {19,8,270}, {19,13,90}, {14,11,180}, {24,11,0} };
    }
    @Override public int[][] getLightPositions() {
        return new int[][]{ {19,9}, {19,11}, {19,13} };
    }
}
