package com.cic.mobapp.data.local;

import com.cic.mobapp.data.local.entity.AnnouncementEntity;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.data.local.entity.ResourceEntity;
import com.cic.mobapp.data.local.entity.UserEntity;
import java.util.Arrays;
import java.util.List;

public final class SeedData {

    private SeedData() {}

    // ── Members ───────────────────────────────────────────────────────────────

    public static List<UserEntity> users() {
        return Arrays.asList(
            // HR
            u("mem_001", "Amine Aissou",           "",                            "amine_._1",                   "Member",        150, "2024-10-05"),
            u("mem_002", "Raid Kahl Ras",           "",                            "",                            "Member",         80, "2024-10-08"),
            u("mem_003", "Farouk Abdelmoumen",      "",                            "",                            "Member",        200, "2024-10-10"),
            u("mem_004", "Amine Belabas",           "",                            "",                            "Member",         90, "2024-10-12"),
            u("mem_005", "Aymen Bouhdjiala",        "",                            "aymenbb7",                    "Member",        120, "2024-10-15"),
            u("mem_044", "Manar Oukili",            "oukilimanar284@gmail.com",    "",                            "Member",        140, "2024-11-20"),
            u("mem_050", "Meriem Belkadi",          "",                            "meriem__",                    "Member",        145, "2024-12-01"),
            u("mem_053", "Ayoub Hendei",            "",                            "youbi_william",               "Member",        115, "2024-12-10"),

            // ER
            u("mem_006", "Houcine Ait Amer Meziane","",                            "lhou7212",                    "Member",        180, "2024-10-18"),
            u("mem_007", "Abdenour Gheribi",        "",                            "",                            "Member",         60, "2024-10-20"),
            u("mem_008", "Loubna Terfaoui",         "",                            "loubnaterfaoui",              "Member",        250, "2024-10-22"),
            u("mem_009", "Rabeh Haddadi",           "",                            "rabeh_hdd",                   "Member",        300, "2024-10-25"),
            u("mem_043", "Islem Yacine Bennouar",   "",                            "xshialoh",                    "Member",        270, "2024-11-18"),
            u("mem_046", "Lidia Ouzine",            "",                            "",                            "Member",         85, "2024-11-28"),
            u("mem_051", "Yasmine Ouchen",          "",                            "lilith2338",                  "Mentor",        580, "2024-12-03"),

            // Design
            u("mem_010", "Cerine Labdi",            "",                            "cerine__2",                   "Mentor",        850, "2024-10-01"),
            u("mem_011", "Amir Mekroud",            "",                            "",                            "Member",        140, "2024-11-01"),
            u("mem_012", "Hanine Ait Amokhtar",     "",                            "",                            "Member",        110, "2024-11-05"),

            // Multi-media
            u("mem_013", "Mohamed Taib Kahoul",     "",                            "mohamedkahloul",              "Member",        220, "2024-11-08"),
            u("mem_014", "Dikra Semmouk",           "",                            "dikrasmk_85202",              "Member",        170, "2024-11-10"),
            u("mem_015", "Ritadj Belayadi",         "",                            "ritedj0283_50053",            "Mentor",        720, "2024-10-03"),
            u("mem_016", "Hadjer Rahai",            "",                            "hadjerrahai",                 "Member",        190, "2024-11-12"),
            u("mem_038", "Romaissa Ait Kassi",      "",                            "romaissa_ak",                 "Member",        240, "2024-11-25"),
            u("mem_042", "Meriem Laazazi",          "",                            "meriem05289",                 "Member",        175, "2024-11-15"),

            // IT
            u("mem_017", "Abdelilah Zengla",        "",                            "abdou1901",                   "Mentor",        680, "2024-10-02"),
            u("mem_018", "Tarek Laib",              "",                            "",                            "Member",         75, "2024-11-14"),
            u("mem_019", "Nabil Khaled",            "",                            "nabil.2897",                  "Member",        320, "2024-11-16"),
            u("mem_021", "Haithem Bouzid",          "",                            "haithem_bouzid",              "Member",        420, "2024-11-02"),
            u("mem_022", "Mohamed Sadek Fehis",     "",                            "",                            "Member",        280, "2024-11-03"),
            u("mem_023", "Mohamed Sadek Abbaci",    "",                            "lmouhb8",                     "Member",        310, "2024-11-04"),
            u("mem_024", "Sami Youcef Harnane",     "",                            "kastx76",                     "Mentor",        780, "2024-10-04"),
            u("mem_026", "Abdelhak Sellah",         "",                            "abdelhak.over.heaven",        "Member",        450, "2024-11-06"),
            u("mem_027", "Abderahim Cherfaoui",     "",                            "",                            "Mentor",        640, "2024-10-06"),
            u("mem_028", "Reda Lahcani",            "",                            "",                            "Member",        130, "2024-11-22"),
            u("mem_029", "Taha Zaidi",              "",                            "",                            "Member",         95, "2024-11-24"),
            u("mem_030", "Abdenour Saifi",          "",                            "",                            "Member",         70, "2024-11-26"),
            u("mem_031", "Mohamed Benzidoun",       "",                            "mohammed_benzidoun007",        "Member",        380, "2024-11-07"),
            u("mem_032", "Mays Taibi",              "",                            "mays_48740",                  "Member",        260, "2024-11-09"),
            u("mem_033", "Mahdi Dahmani",           "",                            "mahdi._.dhmn9227",            "Member",        340, "2024-11-11"),
            u("mem_034", "Mohamed Achouri",         "",                            "",                            "Member",        160, "2024-11-13"),
            u("mem_035", "Farouk Abdelmoumen",      "",                            "",                            "Member",        210, "2024-11-30"),
            u("mem_036", "Fateh Hassani",           "",                            "fateh_r15",                   "Administrator",2500, "2024-09-01"),
            u("mem_039", "Yacer Baghdouche",        "",                            "yacer0764_53213",             "Member",        290, "2024-11-27"),
            u("mem_041", "Yanis Maameri",           "",                            "yan1s",                       "Member",        330, "2024-12-05"),
            u("mem_054", "Abderaouf Guettaia",      "",                            "guettayamohamedabderaouf",    "Member",        360, "2024-12-12"),

            // EH
            u("mem_020", "Belkacem Medjitna",       "",                            "kassem101",                   "Mentor",        950, "2024-10-07"),
            u("mem_025", "Moncef Yaakoubi",         "",                            "monomoncef_1818",             "Mentor",        890, "2024-10-09"),
            u("mem_037", "Tinhnen Gahlouz",         "",                            "",                            "Mentor",        710, "2024-10-11"),
            u("mem_045", "Nadhira Baadi",           "",                            "nadhira_baadi._59124",        "Member",        195, "2024-11-23"),
            u("mem_047", "Djouhaina Belhadj",       "",                            "",                            "Member",        100, "2024-11-29"),
            u("mem_049", "Hemssa Berrahia",         "",                            "",                            "Mentor",        530, "2024-12-06"),
            u("mem_052", "Farouk Hadj Hacene",      "",                            "f4roukk",                     "Member",        200, "2024-12-07"),

            // Finance
            u("mem_040", "Roudjina Assil Kaabeche", "kaabecheassil@gmail.com",    "",                            "Member",        185, "2024-11-19"),

            // Logistics
            u("mem_048", "Alaa Siafa",              "",                            "lola7ll95",                   "Member",        155, "2024-12-02")
        );
    }

    private static UserEntity u(String id, String username, String email,
                                 String discordId, String role, int xp, String joined) {
        UserEntity e = new UserEntity();
        e.id        = id;
        e.username  = username;
        e.email     = email.isEmpty() ? null : email;
        e.discordId = discordId.isEmpty() ? null : discordId;
        e.role      = role;
        e.xp        = xp;
        e.level     = xp / 1000 + 1;
        e.createdAt = joined;
        // Pixel-art avatar seeded by first name
        String firstName = username.split(" ")[0];
        e.avatarUrl = "https://api.dicebear.com/7.x/pixel-art/png?seed=" + firstName + "&size=128";
        return e;
    }

    private static final String ASSET = "file:///android_asset/event_banners/";

    public static List<EventEntity> events() {
        return Arrays.asList(
            e("seed_001", "Open Day",
                "Present the club's projects, activities and vision.\n" +
                "Welcoming new members and student engagement.\n" +
                "Promote collaboration and innovation among students.",
                "Lecture Hall 2 + Classroom", "2025-12-06T14:00:00Z",
                "Event", "Beginner", 200, ASSET + "dsc01886.jpg"),

            e("seed_002", "Introduction to Graphic Design",
                "Trainer: Labdi Cerine",
                "Lecture Hall 1", "2025-12-14T18:00:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01412.jpg"),

            e("seed_003", "Introduction to Ethical Hacking",
                "Trainers: Gahlouz Tinhinene & Harnane Sami Youcef",
                "Lecture Hall 2", "2025-12-15T19:45:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01470.jpg"),

            e("seed_004", "Introduction to Public Speaking",
                "Trainers: Labdi Cerine & Zeraouia Cerine",
                "Lecture Hall 2", "2025-12-16T18:30:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01511.jpg"),

            e("seed_005", "Binary Exploitation & Reverse Engineering",
                "Trainers: Abderrahim Cherfaoui & Zengla Mohammed Abd-elilah",
                "Lecture Hall 2", "2025-12-16T20:00:00Z",
                "Workshop", "Advanced", 50, ASSET + "dsc01516.jpg"),

            e("seed_006", "Introduction to Frontend Fundamentals",
                "Trainer: Belayadi Ritedj",
                "Lecture Hall 2", "2025-12-20T20:00:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01539.jpg"),

            e("seed_010", "Introduction to Frontend Fundamentals",
                "Trainer: Belayadi Ritaj",
                "Lecture Hall 1", "2026-01-25T19:30:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01598.jpg"),

            e("seed_007", "Python Backend Dev: Your First Step",
                "Trainer: Yakoubi Moncef",
                "Lecture Hall 2", "2026-01-26T20:00:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01670.jpg"),

            e("seed_008", "Introduction to Game Dev",
                "Trainer: Bouagual Nour El Islam",
                "Lecture Hall 2", "2026-01-27T18:00:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01681.jpg"),

            e("seed_009", "Design",
                "Trainer: Labdi Cerine",
                "Lecture Hall 2", "2026-01-27T20:00:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01804.jpg"),

            e("seed_011", "Introduction to Game Dev",
                "Trainer: Bouagual Nour El Islam",
                "Lecture Hall 1", "2026-02-01T18:00:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01451.jpg"),

            e("seed_012", "Design",
                "Trainer: Labdi Cerine",
                "Lecture Hall 1", "2026-02-01T20:00:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01914.jpg"),

            e("seed_013", "Intro to Web Ethical Hacking Security",
                "Trainer: Medjitna Belkacem",
                "Lecture Hall 2", "2026-02-02T20:00:00Z",
                "Workshop", "Intermediate", 50, ASSET + "dsc01919.jpg"),

            e("seed_015", "Career Paths in Computer Science",
                "Speaker: Mr. Berghout",
                "Lecture Hall 1", "2026-02-03T14:00:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01933.jpg"),

            e("seed_014", "Intro To Cryptography & Password Cracking",
                "Trainers: Berrahia Hamssa Oumaima & Ouchen Yasmine",
                "Lecture Hall 2", "2026-02-03T20:00:00Z",
                "Workshop", "Intermediate", 50, ASSET + "dsc01956.jpg"),

            e("seed_016", "All Of AI",
                "Trainer: Manaa Mohaned",
                "Lecture Hall 1", "2026-02-08T18:00:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc02040.jpg"),

            e("seed_017", "Digital Forensics & Ethical Hacking",
                "Trainer: Harnane Samy Youcef",
                "Lecture Hall 1", "2026-02-08T20:00:00Z",
                "Workshop", "Intermediate", 50, ASSET + "dsc02327.jpg"),

            e("seed_018", "Wireless Hacking",
                "Trainer: Gahlou Z Tinhinane",
                "Lecture Hall 2", "2026-02-09T20:00:00Z",
                "Workshop", "Intermediate", 50, ASSET + "dsc01412.jpg"),

            e("seed_019", "OOP",
                "Trainer: Yakoubi Ahmed Moncef",
                "Lecture Hall 1", "2026-02-10T18:00:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01539.jpg"),

            e("seed_020", "Intro to Project Management",
                "Trainer: Aissaoui Amine",
                "Lecture Hall 1", "2026-02-10T20:00:00Z",
                "Workshop", "Beginner", 50, ASSET + "dsc01670.jpg"),

            e("seed_021", "CICONIX",
                "Annual event — approx. 55 hours.\n" +
                "Pedagogical: Active Learning, Bridging the Gap, Soft Skills.\n" +
                "Technical: Core Competencies, Implementation, Vulnerability Analysis.\n" +
                "Open to university students in Computer Science, IT, Electronics and Networking.",
                "Lecture Hall 2 + Classroom + Open Space", "2026-02-12T09:00:00Z",
                "Event", "Intermediate", 100, ASSET + "dsc01511.jpg")
        );
    }

    // ── Announcements ─────────────────────────────────────────────────────────

    public static List<AnnouncementEntity> announcements() {
        return Arrays.asList(
            ann("ann_001", "Welcome to CIC Mobile App",
                "Welcome to the Cyber Innovators Club platform. Explore upcoming workshops, CTF events, and resources. Your journey starts here.",
                "General", "Normal", false, "2025-09-01"),
            ann("ann_002", "CICONIX 2026 — Registration Now Open",
                "Our flagship annual event CICONIX is open for registration! 55+ hours of workshops, CTF challenges, and mentorship sessions. Thursday February 12, 2026.",
                "General", "Important", true, "2025-12-01"),
            ann("ann_003", "December 2025 Workshop Series",
                "Full workshop schedule for December is live: Graphic Design (Dec 14), Ethical Hacking (Dec 15), Public Speaking (Dec 16), Binary Exploitation (Dec 16), Frontend Fundamentals (Dec 20).",
                "Workshop", "Normal", true, "2025-11-28"),
            ann("ann_004", "New CTF Resources Available",
                "20 new cybersecurity resources have been added including labs for Buffer Overflow, SQL Injection, Linux PrivEsc, Active Directory Enumeration and more.",
                "Resource Release", "Normal", false, "2025-12-10"),
            ann("ann_005", "January 2026 Sessions",
                "Frontend Fundamentals (Jan 25), Python Backend Dev (Jan 26), Game Dev (Jan 27 & Feb 1), Design workshops — all open for registration.",
                "Workshop", "Normal", false, "2026-01-10"),
            ann("ann_006", "February 2026 Advanced Track",
                "Web Ethical Hacking (Feb 2), Cryptography & Password Cracking (Feb 3), Career Paths in CS (Feb 3), AI Overview (Feb 8), Digital Forensics (Feb 8), Wireless Hacking (Feb 9), OOP (Feb 10), Project Management (Feb 10).",
                "Workshop", "Important", false, "2026-01-20")
        );
    }

    private static AnnouncementEntity ann(String id, String title, String body,
                                           String type, String priority,
                                           boolean pinned, String date) {
        AnnouncementEntity e = new AnnouncementEntity();
        e.id        = id;
        e.title     = title;
        e.body      = body;
        e.type      = type;
        e.priority  = priority;
        e.isPinned  = pinned;
        e.createdAt = date;
        return e;
    }

    // ── Resources ─────────────────────────────────────────────────────────────

    public static List<ResourceEntity> resources() {
        return Arrays.asList(
            r("res_001", "Linux Privilege Escalation Basics",
                "Lab", "Linux Security", "Beginner",
                "Hands-on lab covering common Linux privilege escalation techniques including SUID binaries, cron jobs, and writable paths.",
                "linux,privesc,suid,cron"),
            r("res_002", "Intro to Web Application Security",
                "Slides", "Web Security", "Beginner",
                "Slide deck introducing core web security concepts: HTTP, cookies, sessions, same-origin policy, and common vulnerability classes.",
                "web,http,owasp,beginner"),
            r("res_003", "OWASP Top 10 Explained",
                "Documentation", "Web Security", "Beginner",
                "Detailed walkthrough of the OWASP Top 10 2021 with real-world examples and mitigation strategies for each vulnerability.",
                "owasp,top10,injection,xss,web"),
            r("res_004", "Burp Suite Essentials",
                "Video", "Penetration Testing", "Intermediate",
                "Video course covering Burp Suite's core tools: Proxy, Repeater, Intruder, and Scanner. Includes hands-on exercises.",
                "burp,proxy,pentesting,web"),
            r("res_005", "Wireshark Packet Analysis Workshop",
                "Lab", "Networks", "Intermediate",
                "Lab-based workshop on using Wireshark to capture and analyze network traffic, filter packets, and detect anomalies.",
                "wireshark,network,pcap,traffic"),
            r("res_006", "Reverse Engineering ELF Binaries",
                "Lab", "Reverse Engineering", "Advanced",
                "Advanced lab covering ELF binary analysis using Ghidra and GDB. Topics include disassembly, patching, and keygen challenges.",
                "reverse,elf,ghidra,gdb,binary"),
            r("res_007", "Cryptography Fundamentals",
                "PDF", "Cryptography", "Beginner",
                "PDF guide covering symmetric and asymmetric cryptography, hash functions, PKI, and common cryptographic attacks.",
                "crypto,aes,rsa,hash,pkcs"),
            r("res_008", "Practical SQL Injection",
                "Lab", "Web Security", "Intermediate",
                "Guided lab series on exploiting SQL injection vulnerabilities including UNION-based, blind, and time-based techniques.",
                "sqli,sql,injection,database,web"),
            r("res_009", "Active Directory Enumeration",
                "Documentation", "Red Teaming", "Advanced",
                "Comprehensive guide to enumerating Active Directory environments using BloodHound, PowerView, and manual techniques.",
                "ad,ldap,bloodhound,redteam,windows"),
            r("res_010", "Building Secure APIs",
                "Slides", "Backend Security", "Intermediate",
                "Slide deck on secure API design: authentication, authorization, rate limiting, input validation, and OWASP API Top 10.",
                "api,rest,jwt,oauth,security"),
            r("res_011", "Intro to Malware Analysis",
                "Video", "Malware Analysis", "Advanced",
                "Video series on static and dynamic malware analysis using REMnux, Cuckoo, and PE Studio. Covers PE format, unpacking, and IOCs.",
                "malware,pe,cuckoo,remnux,analysis"),
            r("res_012", "Android Application Security",
                "PDF", "Mobile Security", "Intermediate",
                "PDF guide on Android app pentesting: APK analysis, Frida hooks, SSL pinning bypass, insecure storage, and intent vulnerabilities.",
                "android,apk,frida,mobile,security"),
            r("res_013", "Docker Security Essentials",
                "Documentation", "Cloud Security", "Intermediate",
                "Documentation on securing Docker deployments: container escapes, image hardening, secrets management, and network policies.",
                "docker,container,cloud,devops,security"),
            r("res_014", "Digital Forensics Basics",
                "Slides", "Forensics", "Beginner",
                "Introduction to digital forensics: evidence acquisition, chain of custody, disk imaging, file system analysis, and timeline creation.",
                "forensics,disk,timeline,evidence"),
            r("res_015", "Buffer Overflow Fundamentals",
                "Lab", "Binary Exploitation", "Advanced",
                "Lab series on stack-based buffer overflows: stack layout, return address overwrite, shellcode injection, and ROP chain basics.",
                "bof,stack,shellcode,exploit,rop"),
            r("res_016", "Secure Coding in C++",
                "PDF", "Secure Development", "Intermediate",
                "PDF covering secure C++ development practices: memory safety, smart pointers, input validation, and common vulnerability patterns.",
                "cpp,memory,secure,development,coding"),
            r("res_017", "Wireless Network Attacks",
                "Video", "Wireless Security", "Advanced",
                "Video course on 802.11 security: WPA2 cracking, evil twin attacks, deauthentication, PMKID attacks, and enterprise Wi-Fi testing.",
                "wifi,wpa2,wireless,aircrack,pmkid"),
            r("res_018", "Threat Modeling Workshop",
                "Documentation", "Security Architecture", "Intermediate",
                "Workshop guide on threat modeling using STRIDE and DREAD methodologies. Includes templates and real-world case studies.",
                "threat,model,stride,dread,architecture"),
            r("res_019", "Intro to SIEM Platforms",
                "Slides", "Blue Team", "Intermediate",
                "Slide deck introducing SIEM platforms (Splunk, ELK). Covers log ingestion, alert rules, dashboards, and incident response workflows.",
                "siem,splunk,elk,blueteam,logs"),
            r("res_020", "Capture The Flag Starter Pack",
                "Lab", "CTF", "Beginner",
                "Beginner-friendly CTF starter pack covering web challenges, crypto puzzles, steganography, and basic reverse engineering tasks.",
                "ctf,web,crypto,stego,beginner")
        );
    }

    private static ResourceEntity r(String id, String title, String type,
                                     String category, String difficulty,
                                     String description, String tags) {
        ResourceEntity e = new ResourceEntity();
        e.id          = id;
        e.title       = title;
        e.type        = type;
        e.category    = category;
        e.difficulty  = difficulty;
        e.description = description;
        e.tags        = tags;
        e.uploadedBy  = "CIC Team";
        e.fileUrl     = "https://example.com/resources/" + id;
        e.createdAt   = "2025-01-01";
        return e;
    }

    private static EventEntity e(String id, String title, String desc,
                                  String location, String date,
                                  String type, String difficulty, int capacity,
                                  String bannerUrl) {
        EventEntity ev = new EventEntity();
        ev.id             = id;
        ev.title          = title;
        ev.description    = desc;
        ev.bannerUrl      = bannerUrl;
        ev.location       = location;
        ev.date           = date;
        ev.type           = type;
        ev.difficulty     = difficulty;
        ev.capacity       = capacity;
        ev.registeredCount = 0;
        ev.isRegistered   = false;
        return ev;
    }
}
