package usst.mud;

/**
 * 校园数据仓库：把场景、NPC、对手、谜题、物品、随机事件
 * 全部用"一维对象数组"存起来，游戏运行时直接遍历这些数组。
 *
 * 对应任务 3：至少 3 条数据用一维对象数组管理，并在运行中实际使用。
 * 这里一共用了 6 个数组：Room[14]、NPC[7]、Monster[6]、
 * Question[8]、Item[16]、CampusEvent[6]。
 */
public final class CampusData {

    public static final int TOTAL_STAMPS = 8;

    // ==================== 场景下标常量 ====================
    public static final int ROOM_GATE = 0;
    public static final int ROOM_ROAD = 1;
    public static final int ROOM_YAN = 2;
    public static final int ROOM_SIPEI = 3;
    public static final int ROOM_HALL = 4;
    public static final int ROOM_GEZHI = 5;
    public static final int ROOM_LAB = 6;
    public static final int ROOM_LIBRARY = 7;
    public static final int ROOM_CANTEEN = 8;
    public static final int ROOM_DORM = 9;
    public static final int ROOM_PLAYGROUND = 10;
    public static final int ROOM_HOSPITAL = 11;
    public static final int ROOM_MARKET = 12;
    public static final int ROOM_MACHINE = 13;

    // ==================== 一维对象数组：物品 ====================
    public static final Item[] ITEMS = {
            new Item("baozi", "肉包", "食堂刚出笼的肉包，还冒着热气", Item.Type.FOOD, 25, 0),
            new Item("coffee", "咖啡", "便利店冰柜里的罐装咖啡", Item.Type.FOOD, 15, 0),
            new Item("redbull", "红牛", "熬夜专用，喝下去心跳都快了", Item.Type.FOOD, 35, 0),
            new Item("catfood", "猫粮", "教育超市角落里的猫粮，校猫最爱", Item.Type.QUEST, 0, 0),
            new Item("photo", "老照片", "思裴堂信箱里的黑白照片，背面写着 1987", Item.Type.QUEST, 0, 0),
            new Item("campus_card", "校园卡", "印着学号的校园卡，食堂和图书馆都要用", Item.Type.TOOL, 0, 0),
            new Item("wrongbook", "错题本", "学长留下的错题本，翻一翻能涨学分", Item.Type.TOOL, 0, 3),
            new Item("book", "校史手册", "湛恩图书馆编的校史小册子，字里行间都是故事", Item.Type.TOOL, 0, 5),
            new Item("stamp_gate", "印章·密氏校门", "一枚刻着老校门的铜印，是七枚印章的第一枚", Item.Type.STAMP, 0, 0),
            new Item("stamp_road", "印章·湛恩大道", "校猫叼来的印章，沾着一点猫毛", Item.Type.STAMP, 0, 0),
            new Item("stamp_yan", "印章·思晏堂", "从四六级词灵身上掉下来的印章", Item.Type.STAMP, 0, 0),
            new Item("stamp_sipei", "印章·思裴堂", "藏在思裴堂旧信箱底部的印章", Item.Type.STAMP, 0, 0),
            new Item("stamp_hall", "印章·大礼堂", "社团学长郑重交给你的印章", Item.Type.STAMP, 0, 0),
            new Item("stamp_gezhi", "印章·格致堂", "高数幽灵消散后露出的印章", Item.Type.STAMP, 0, 0),
            new Item("stamp_lib", "印章·湛恩图书馆", "管理员从抽屉最深处取出的印章", Item.Type.STAMP, 0, 0),
            new Item("stamp_final", "印章·选课系统", "最后一枚印章，表面印着不断滚动的课程代码", Item.Type.STAMP, 0, 0)
    };

    // ==================== 一维对象数组：谜题 ====================
    public static final Question[] QUESTIONS = {
            new Question("上海理工大学的校训是「信义勤爱，______」，请补全后四个字。",
                    new String[]{"思学志远"}, "校训共八个字，后四个讲的是求学与志向。", 5),
            new Question("湛恩纪念图书馆纪念的是哪位校长？",
                    new String[]{"刘湛恩"}, "他是沪江大学第一任华人校长，1928 年就职。", 5),
            new Question("校园里第一座大型建筑，清水红砖、学院哥特式风格的是哪座楼？",
                    new String[]{"思晏堂"}, "它纪念的是晏马太博士。", 6),
            new Question("1937 年落成、与思魏堂连体呈 L 形的是哪座建筑？",
                    new String[]{"大礼堂", "大礼堂·思魏堂", "思魏堂"}, "它是抗战爆发前校园里最后一栋落成的楼。", 5),
            new Question("格致堂的前身是什么？",
                    new String[]{"科学馆", "沪江大学科学馆"}, "「格物致知」，和科学有关。", 4),
            new Question("密氏校门是为纪念哪位教授而命名的？",
                    new String[]{"米拉德", "密氏"}, "他的英文姓是 Millard。", 4),
            new Question("一门课每周 3 节，一学期 18 周，一共多少节课？",
                    new String[]{"54", "五十四"}, "3 × 18。", 3),
            new Question("图书馆 8:00 开门、22:00 关门，一天开放多少小时？",
                    new String[]{"14", "十四"}, "22 - 8。", 3)
    };

    // ==================== 一维对象数组：随机校园事件 ====================
    public static final CampusEvent[] EVENTS = {
            new CampusEvent("梧桐叶落", "一片梧桐叶打着旋落在你肩上，秋天到了。", 0, 0, null),
            new CampusEvent("辅导员查课", "辅导员从拐角冒出来点名，你解释了半天，口干舌燥。", -5, 0, null),
            new CampusEvent("蹭到一场讲座", "你溜进报告厅听了半场讲座，居然还挺有收获。", 0, 3, null),
            new CampusEvent("校猫蹭腿", "一只橘白校猫绕着你转了两圈，你蹲下来撸了十分钟。", 5, 0, null),
            new CampusEvent("突降暴雨", "一场太阳雨毫无征兆地砸下来，你抱着书包一路狂奔。", -4, 0, null),
            new CampusEvent("捡到一张校园卡", "你在路边捡到一张校园卡，决定先收着，等失主找来。", 0, 0, "campus_card")
    };

    // ==================== 一维对象数组：场景 ====================
    public static final Room[] ROOMS = new Room[14];

    // ==================== 一维对象数组：NPC ====================
    public static final NPC[] NPCS = new NPC[7];

    // ==================== 一维对象数组：对手 ====================
    public static final Monster[] MONSTERS = new Monster[6];

    static {
        buildRooms();
        buildNpcs();
        buildMonsters();
    }

    private CampusData() {
    }

    private static void buildRooms() {
        ROOMS[ROOM_GATE] = new Room(ROOM_GATE, "密氏校门",
                "两根苏州花岗岩立柱撑起的老校门，门额上「上海理工大学」几个字在晨光里发亮。门外是军工路，车流不断；门内是湛恩大道，梧桐叶落了一地。保安大叔端着搪瓷杯打量你。",
                "校门 1916 年建成，1992 年建校 86 周年时按原样放大 1.23 倍重建。",
                null, null, false);

        ROOMS[ROOM_ROAD] = new Room(ROOM_ROAD, "湛恩大道",
                "笔直的大道正对校门，两侧是法国梧桐和清水红砖的老楼。路的尽头，湛恩纪念图书馆的轮廓安静地立着。一只橘白相间的校猫蹲在路边舔爪子。",
                "这条路以刘湛恩校长命名，是校园的中轴线。",
                null, null, false);

        ROOMS[ROOM_YAN] = new Room(ROOM_YAN, "思晏堂",
                "校园里第一座大型建筑，清水红砖、双坡红瓦，学院哥特式的尖顶下是长长的走廊。教室里传来翻书声，也传来一阵念念有词——像是有人在背单词，又像是什么东西在磨牙。",
                "思晏堂是沪江大学的开篇之作，纪念晏马太博士。",
                null, null, false);

        ROOMS[ROOM_SIPEI] = new Room(ROOM_SIPEI, "思裴堂",
                "1915 年落成的老楼，一层是教室，上面三层曾经是男生寝室。木楼梯踩上去吱呀作响，墙角的旧信箱里塞满了旧时光。",
                "思裴堂纪念裴理克博士，百年前的学生在这里上课、住宿。",
                new String[]{"photo"}, "stamp_sipei", false);

        ROOMS[ROOM_HALL] = new Room(ROOM_HALL, "大礼堂·思魏堂",
                "L 形的连体建筑，后罗马风格的拱窗。1937 年为建校 30 周年而建，也是抗战爆发前校园里最后一栋楼。今天礼堂门口摆着社团招新的长桌。",
                "大礼堂与纪念魏馥兰校长的思魏堂连为一体。",
                null, null, false);

        ROOMS[ROOM_GEZHI] = new Room(ROOM_GEZHI, "格致堂",
                "前身是沪江大学科学馆。「格物致知」四个字刻在门楣上，走廊里飘着焊锡和咖啡混合的味道。实验室的灯，这个点还亮着。",
                "格致堂的旧称是科学馆，上理的工科血脉从这里开始。",
                new String[]{"wrongbook"}, null, false);

        ROOMS[ROOM_LAB] = new Room(ROOM_LAB, "光电实验室",
                "日光灯嗡嗡作响，示波器的绿光一闪一闪。桌上摊着没写完的实验报告，保温杯里的咖啡早就凉了。在这里刷题效率很高，也很容易忘记时间。",
                "格致堂三楼的实验室，通宵的传说都发生在这里。",
                new String[]{"coffee"}, null, false);

        ROOMS[ROOM_LIBRARY] = new Room(ROOM_LIBRARY, "湛恩纪念图书馆",
                "1928 年刘湛恩校长就职当天为之破土的老图书馆。旋转楼梯、木质书架、磨得发亮的阅览桌，空气里有纸张和旧木头的味道。",
                "一楼大厅挂着历任校长的照片，楼梯通向顶层的机房。",
                new String[]{"book"}, null, false);

        ROOMS[ROOM_CANTEEN] = new Room(ROOM_CANTEEN, "第一食堂",
                "中午的队伍能排到门口。打菜窗口的玻璃上贴着「今日推荐」，阿姨的勺子稳稳地悬在红烧肉上方。",
                "食堂是最快恢复体力的地方，前提是错过饭点高峰。",
                new String[]{"baozi"}, null, true);

        ROOMS[ROOM_DORM] = new Room(ROOM_DORM, "学生宿舍",
                "六层楼，走廊里晾着被子，门口堆着快递盒。你的床在上铺，桌上还留着昨晚的泡面桶。宿管大爷在传达室看报纸。",
                "这是你的复活点：体力透支后会被送回这里。",
                new String[]{"campus_card"}, null, true);

        ROOMS[ROOM_PLAYGROUND] = new Room(ROOM_PLAYGROUND, "操场",
                "塑胶跑道被夕阳烤得发烫，有人在跑步，有人在踢球，还有人在体测补测。看台背面的墙上写着「每天锻炼一小时」。",
                "体测怪常年盘踞在单杠附近，据说它专挑八百米测试时出现。",
                new String[]{"redbull"}, null, false);

        ROOMS[ROOM_HOSPITAL] = new Room(ROOM_HOSPITAL, "校医院",
                "白色的走廊，消毒水味道。挂号窗口的阿姨头也不抬：「哪里不舒服？」",
                "校医院可以休息，也是传说中另一个复活点。",
                new String[]{"baozi"}, null, true);

        ROOMS[ROOM_MARKET] = new Room(ROOM_MARKET, "教育超市",
                "货架上从牙刷到实验报告纸一应俱全。收银台边贴着「校园卡充值处」，角落里堆着几袋猫粮。",
                "超市的猫粮是校猫最认的东西。",
                new String[]{"catfood", "coffee", "baozi", "redbull"}, "catfood", false);

        ROOMS[ROOM_MACHINE] = new Room(ROOM_MACHINE, "选课系统机房",
                "图书馆顶层的机房，空调冷得像冰窖。一排排服务器指示灯有节奏地闪烁，屏幕上滚动着「选课系统维护中」。空气里有一种被算法注视的感觉。",
                "门禁只认 6 枚印章的能量，剩下的秘密在里面。",
                null, null, false);

        // ===== 连接出口（下标 -> 下标），构成一张连通的校园地图 =====
        ROOMS[ROOM_GATE].setExit("north", ROOM_ROAD);

        ROOMS[ROOM_ROAD].setExit("south", ROOM_GATE);
        ROOMS[ROOM_ROAD].setExit("north", ROOM_LIBRARY);
        ROOMS[ROOM_ROAD].setExit("east", ROOM_YAN);
        ROOMS[ROOM_ROAD].setExit("west", ROOM_CANTEEN);

        ROOMS[ROOM_YAN].setExit("west", ROOM_ROAD);
        ROOMS[ROOM_YAN].setExit("north", ROOM_SIPEI);
        ROOMS[ROOM_YAN].setExit("east", ROOM_HALL);

        ROOMS[ROOM_SIPEI].setExit("south", ROOM_YAN);
        ROOMS[ROOM_SIPEI].setExit("east", ROOM_GEZHI);

        ROOMS[ROOM_HALL].setExit("west", ROOM_YAN);
        ROOMS[ROOM_HALL].setExit("north", ROOM_PLAYGROUND);

        ROOMS[ROOM_GEZHI].setExit("west", ROOM_SIPEI);
        ROOMS[ROOM_GEZHI].setExit("north", ROOM_LAB);
        ROOMS[ROOM_GEZHI].setExit("east", ROOM_MARKET);

        ROOMS[ROOM_LAB].setExit("south", ROOM_GEZHI);

        ROOMS[ROOM_LIBRARY].setExit("south", ROOM_ROAD);
        ROOMS[ROOM_LIBRARY].setExit("up", ROOM_MACHINE);

        ROOMS[ROOM_CANTEEN].setExit("east", ROOM_ROAD);
        ROOMS[ROOM_CANTEEN].setExit("north", ROOM_DORM);

        ROOMS[ROOM_DORM].setExit("south", ROOM_CANTEEN);
        ROOMS[ROOM_DORM].setExit("east", ROOM_HOSPITAL);

        ROOMS[ROOM_PLAYGROUND].setExit("south", ROOM_HALL);

        ROOMS[ROOM_HOSPITAL].setExit("west", ROOM_DORM);

        ROOMS[ROOM_MARKET].setExit("west", ROOM_GEZHI);

        ROOMS[ROOM_MACHINE].setExit("down", ROOM_LIBRARY);
    }

    private static void buildNpcs() {
        NPCS[0] = new NPC("保安大叔", "校门保安", ROOM_GATE,
                new String[]{"小伙子，学生证带了吗？", "这门楼比你我年纪都大，站好喽。", "往里走，别在门口堵着。"},
                null, "stamp_gate", 2, 0, "把学生证给我看看。");

        NPCS[1] = new NPC("校猫", "橘白猫", ROOM_ROAD,
                new String[]{"喵。", "喵喵——（它尾巴一甩，指向图书馆的方向）", "（它打了个哈欠，露出两颗小尖牙）"},
                "catfood", "stamp_road", 2, 0, "它盯着你的口袋，又看了看教育超市的方向。");

        NPCS[2] = new NPC("老学长", "思裴堂常客", ROOM_SIPEI,
                new String[]{"这楼里的信箱，几十年没换过。", "我读书那会儿，也在这条走廊上背过单词。", "印章啊……找东西要往旧的地方找。"},
                null, null, 3, 0, "先四处翻翻，别急着走。");

        NPCS[3] = new NPC("社团学长", "辩论社社长", ROOM_HALL,
                new String[]{"辩论社缺个能接话的，你要不要来试试？", "答对两题我就把印章给你，说话算话。", "逻辑是练出来的，不是想出来的。"},
                null, "stamp_hall", 5, 2, "先去答对两题（quiz），再来找我。");

        NPCS[4] = new NPC("图书馆管理员", "三楼借阅台", ROOM_LIBRARY,
                new String[]{"安静。", "这套校史，全上海只有我们这里最全。", "答对四道题的人，才有资格拿走那枚印章。"},
                null, "stamp_lib", 6, 4, "答对四道题，再来借阅台找我。");

        NPCS[5] = new NPC("宿管大爷", "一号楼传达室", ROOM_DORM,
                new String[]{"被子收了吗？", "年轻人，别老熬夜。", "熄灯以后走廊里别跑。"},
                null, null, 0, 0, "……");

        NPCS[6] = new NPC("食堂阿姨", "三号窗口", ROOM_CANTEEN,
                new String[]{"要辣吗？", "肉包刚出笼，趁热。", "同学，饭卡刷一下。"},
                null, "baozi", 0, 0, "……");
    }

    private static void buildMonsters() {
        MONSTERS[0] = new Monster("四六级词灵", ROOM_YAN, 30, 5, 1, 8,
                "stamp_yan", false, 0,
                "一团由 abandon、ambiguous、anticipate 组成的词云堵住了走廊，它正反复念着你没背下来的单词。");

        MONSTERS[1] = new Monster("拖延兽", ROOM_SIPEI, 35, 6, 1, 6,
                "coffee", false, 0,
                "一只毛茸茸的怪物趴在你没写完的作业上，每当你靠近它就翻个身：「明天再写嘛。」");

        MONSTERS[2] = new Monster("高数幽灵", ROOM_GEZHI, 40, 7, 2, 10,
                "stamp_gezhi", false, 0,
                "黑板上浮出一个由极限符号组成的人影，它举起粉笔：「证明一下，这个级数收敛。」");

        MONSTERS[3] = new Monster("DDL 魅影", ROOM_LIBRARY, 45, 8, 2, 10,
                "book", false, 0,
                "时钟指向 23:59，一个没有面孔的影子从书架间滑出来，手里攥着一长串截止日期。");

        MONSTERS[4] = new Monster("体测怪", ROOM_PLAYGROUND, 38, 7, 1, 8,
                "redbull", false, 0,
                "单杠下面站着一个肌肉发达的身影，它晃了晃秒表：「八百米，现在开始。」");

        MONSTERS[5] = new Monster("选课系统·熵", ROOM_MACHINE, 80, 7, 3, 20,
                "stamp_final", true, 6,
                "服务器指示灯同时变成红色，屏幕上滚出一行字：「欢迎来到选课，祝你抢到课。」");
    }

    // ==================== 数组查找工具方法 ====================

    /** 按 id 在 ITEMS 数组里查找物品 */
    public static Item itemById(String id) {
        for (int i = 0; i < ITEMS.length; i++) {
            if (ITEMS[i].getId().equals(id)) {
                return ITEMS[i];
            }
        }
        return null;
    }

    /** 在指定场景里按名字找 NPC */
    public static NPC findNpc(int roomId, String name) {
        for (int i = 0; i < NPCS.length; i++) {
            if (NPCS[i].getRoomId() != roomId) {
                continue;
            }
            if (name == null || NPCS[i].getName().contains(name) || name.contains(NPCS[i].getName())) {
                return NPCS[i];
            }
        }
        return null;
    }

    /** 找出指定场景中还没被击败的对手 */
    public static Monster monsterInRoom(int roomId) {
        for (int i = 0; i < MONSTERS.length; i++) {
            if (MONSTERS[i].getRoomId() == roomId && !MONSTERS[i].isDefeated()) {
                return MONSTERS[i];
            }
        }
        return null;
    }

    /** 统计已击败的对手数量（数组遍历，对应任务 3 的统计要求） */
    public static int defeatedCount() {
        int count = 0;
        for (int i = 0; i < MONSTERS.length; i++) {
            if (MONSTERS[i].isDefeated()) {
                count++;
            }
        }
        return count;
    }
}
