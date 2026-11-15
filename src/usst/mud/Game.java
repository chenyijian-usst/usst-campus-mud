package usst.mud;

import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

/**
 * 游戏主控：负责游戏循环、时间推进、场景描述、战斗与谜题的调度。
 *
 * 注意：指令的"判断"不在这里，而在 CommandProcessor 中；
 * Game 只提供"做什么"的方法（move / talk / search / ...），
 * 这样 main 里就不会堆满 if-else。
 */
public class Game {

    private static final int MOVE_MINUTES = 5;
    private static final int TALK_MINUTES = 5;
    private static final int SEARCH_MINUTES = 10;
    private static final int QUIZ_MINUTES = 5;
    private static final int STUDY_MINUTES = 30;
    private static final int REST_MINUTES = 30;
    private static final int ANSWER_MINUTES = 2;
    private static final int STUDY_ENERGY_COST = 25;
    private static final int REST_ENERGY_GAIN = 30;
    private static final int MACHINE_STAMP_REQUIREMENT = 6;

    private final Scanner scanner;
    private final Random random;
    private final boolean echoInput;

    private Player player;
    private Battle battle;
    private Question activeQuestion;
    private boolean running;
    private int day;
    private int clock;   // 从 0:00 起算的分钟数

    public Game() {
        this(new Random(), Boolean.getBoolean("mud.echo"));
    }

    /** 指定随机种子，方便在报告/答辩时复现同一场随机事件 */
    public Game(long seed) {
        this(new Random(seed), Boolean.getBoolean("mud.echo"));
    }

    private Game(Random random, boolean echoInput) {
        this.scanner = new Scanner(System.in, "UTF-8");
        this.random = random;
        this.echoInput = echoInput;
        this.day = 1;
        this.clock = 8 * 60;
        this.running = true;
    }

    public static void main(String[] args) {
        Long seed = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && args[i].startsWith("--seed=")) {
                try {
                    seed = Long.parseLong(args[i].substring("--seed=".length()));
                } catch (NumberFormatException ignored) {
                    // 参数写错时退回默认随机种子
                }
            }
        }
        Game game = seed == null ? new Game() : new Game(seed);
        game.start();
    }

    // ==================== 游戏主循环 ====================

    public void start() {
        printBanner();
        String name = askName();
        player = new Player(name,
                CampusData.ROOMS[CampusData.ROOM_GATE],
                CampusData.ROOMS[CampusData.ROOM_DORM]);
        player.getBag().add(CampusData.itemById("campus_card"));
        player.getBag().add(CampusData.itemById("baozi"));

        out("");
        out("你好，" + player.getName() + "。你现在站在密氏校门口。");
        out("输入 help 查看指令，输入 look 看看四周。");
        look();

        while (running) {
            System.out.print("\n[第" + day + "天 " + clockText()
                    + " 体力" + player.getEnergy()
                    + " 行动" + player.getActionsLeft() + "/" + Player.DAILY_ACTIONS + "] > ");
            System.out.flush();
            String line = readLine();
            if (line == null) {
                out("");
                out("（输入结束，游戏退出。）");
                break;
            }
            if (echoInput) {
                out(line);   // 以文件重定向方式演示时，把输入回显出来，方便写进报告
            }
            CommandProcessor.processCommand(line, this);
        }
        out("再见，祝你绩点 4.0！");
    }

    private void printBanner() {
        out("============================================================");
        out("              上 理 江 湖 · 沪 江 迷 踪");
        out("      上海理工大学（军工路校区）文字 MUD · 单机版");
        out("============================================================");
        out("开学第一周，你的储物柜里多了一张没有署名的纸条：");
        out("");
        out("    「集齐八枚沪江印章，到湛恩图书馆顶层来。」");
        out("");
        out("纸条背面印着一行小字：门禁只认六枚印章的能量。");
    }

    private String askName() {
        out("");
        out("请输入你的名字（直接回车使用默认名「林小满」）：");
        for (int i = 0; i < 3; i++) {
            System.out.print("名字> ");
            System.out.flush();
            String line = readLine();
            if (line == null) {
                return "林小满";
            }
            line = line.replace("\uFEFF", "").trim();
            if (line.isEmpty()) {
                return "林小满";
            }
            if (line.length() > 12) {
                out("名字最多 12 个字符，请重新输入。");
                continue;
            }
            return line;
        }
        return "林小满";
    }

    private String readLine() {
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    // ==================== 输出工具 ====================

    public void out(String text) {
        System.out.println(text);
    }

    private String clockText() {
        int h = clock / 60;
        int m = clock % 60;
        return String.format("%02d:%02d", h, m);
    }

    // ==================== 状态查询 ====================

    public boolean isInBattle() {
        return battle != null && !battle.isOver();
    }

    public Player getPlayer() {
        return player;
    }

    // ==================== 观察 / 地图 / 背包 ====================

    public void look() {
        Room room = player.getCurrentRoom();
        out("");
        out(room.toString());
        if (!room.getFeature().isEmpty()) {
            out("（" + room.getFeature() + "）");
        }
        out(room.exitsText());

        // 遍历 NPC 数组，找出当前场景里的人
        boolean hasNpc = false;
        for (int i = 0; i < CampusData.NPCS.length; i++) {
            NPC npc = CampusData.NPCS[i];
            if (npc.getRoomId() == room.getId()) {
                if (!hasNpc) {
                    out("这里的人：");
                    hasNpc = true;
                }
                out("  · " + npc.getRole() + "「" + npc.getName() + "」（talk " + npc.getName() + "）");
            }
        }

        // 遍历对手数组，找出还没被击败的对手
        for (int i = 0; i < CampusData.MONSTERS.length; i++) {
            Monster monster = CampusData.MONSTERS[i];
            if (monster.getRoomId() == room.getId() && !monster.isDefeated()) {
                out("⚠ 对手：「" + monster.getName() + "」气势 " + monster.getHp()
                        + "/" + monster.getMaxHp() + "（fight 挑战）");
            }
        }

        if (room.getId() == CampusData.ROOM_MACHINE && player.getStampCount() < MACHINE_STAMP_REQUIREMENT) {
            out("⛔ 门禁红灯：需要 " + MACHINE_STAMP_REQUIREMENT + " 枚印章，你只有 "
                    + player.getStampCount() + " 枚。");
        }
    }

    public void showMap() {
        out("");
        out("【校园地图】共 " + CampusData.ROOMS.length + " 个场景（方括号里是数组下标）");
        for (int i = 0; i < CampusData.ROOMS.length; i++) {
            Room room = CampusData.ROOMS[i];
            String marker = (room.getId() == player.getCurrentRoom().getId()) ? " ← 你在这里" : "";
            out(String.format("[%2d] ", room.getId()) + padRight(room.getName(), 16)
                    + room.exitsText() + marker);
        }
    }

    /** 按"显示宽度"补空格：一个汉字占两格，否则中文表格会歪 */
    private static String padRight(String text, int width) {
        StringBuilder sb = new StringBuilder(text);
        int current = displayWidth(text);
        while (current < width) {
            sb.append(' ');
            current++;
        }
        return sb.toString();
    }

    private static int displayWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            width += (c >= 0x2E80 && c <= 0x9FFF) ? 2 : 1;
        }
        return width;
    }

    public void showBag() {
        out("");
        out("【背包】" + player.getBag().list());
        out("印章：" + player.getStampCount() + "/" + CampusData.TOTAL_STAMPS);
        out("提示：use 物品名 可以使用食物或道具。");
    }

    public void status() {
        out("");
        out(player.toString());
        out("时间：第 " + day + " 天 " + clockText());
        out("校园里已被击败的对手：" + CampusData.defeatedCount() + "/" + CampusData.MONSTERS.length);
    }

    public void help() {
        out("");
        out("【可用指令】（不区分大小写，首尾空格会被忽略）");
        out("  help / 帮助            查看本帮助");
        out("  look / 看              观察当前场景");
        out("  go 方向 / 走 方向       移动：north south east west up down（或 n s e w u d / 北南东西上下）");
        out("  status / 状态           查看角色状态面板");
        out("  map / 地图              查看整个校园地图");
        out("  bag / 背包              查看背包");
        out("  talk 名字 / 对话        与场景中的 NPC 交谈");
        out("  search / 搜             搜索当前场景，可能找到物品或印章");
        out("  quiz / 答题             抽一道校园谜题");
        out("  answer 答案 / 答        回答当前谜题");
        out("  hint / 提示             查看当前谜题的提示");
        out("  study / 自习            刷题：消耗体力，换取学分");
        out("  rest / 休息             在宿舍 / 食堂 / 校医院恢复体力");
        out("  fight / 挑战            与当前场景的对手对决");
        out("  attack / defend / flee  战斗中的进攻、防守、逃跑");
        out("  use 物品 / 用           使用背包里的物品");
        out("  quit / 退出             退出游戏");
        out("小技巧：直接输入 north 或 北 也能移动；输入空行会得到提示而不是报错。");
    }

    // ==================== 移动 ====================

    public void move(String direction) {
        int dirIndex = Room.directionIndex(direction);
        if (dirIndex < 0) {
            out("「" + direction + "」不是一个方向。试试 north / south / east / west / up / down。");
            return;
        }
        Room current = player.getCurrentRoom();
        int targetId = current.getExit(dirIndex);

        // 越界检查 1：这个方向根本没有出口
        if (targetId < 0) {
            out("你朝" + Room.DIRECTION_CN[dirIndex] + "看了看——「" + current.getName()
                    + "」的" + Room.DIRECTION_CN[dirIndex] + "边没有路。");
            return;
        }
        // 越界检查 2：出口数据本身不合法（防御性编程，正常数据不会触发）
        if (targetId >= CampusData.ROOMS.length) {
            out("【系统】出口数据异常：目标场景下标 " + targetId + " 越界，已阻止本次移动。");
            return;
        }

        Room target = CampusData.ROOMS[targetId];

        // 机房有门禁：印章不够不让进
        if (target.getId() == CampusData.ROOM_MACHINE
                && player.getStampCount() < MACHINE_STAMP_REQUIREMENT) {
            out("你踏上顶层的楼梯，门禁发出两声短促的蜂鸣。");
            out("屏幕上显示：印章能量不足（需要 " + MACHINE_STAMP_REQUIREMENT
                    + " 枚，你持有 " + player.getStampCount() + " 枚）。");
            return;
        }

        if (!spendTurn(MOVE_MINUTES)) {
            return;
        }
        String notice = player.moveTo(target);
        out(notice);

        if (player.hasJustCollapsed()) {
            out("（你没能走到目的地，醒来时已经在宿舍了。）");
            look();
            return;
        }
        look();
        maybeRandomEvent();
    }

    /** 任务 5 的 Random 应用之一：移动时有 40% 概率触发随机校园事件 */
    private void maybeRandomEvent() {
        if (random.nextInt(100) >= 40) {
            return;
        }
        CampusEvent event = CampusData.EVENTS[random.nextInt(CampusData.EVENTS.length)];
        out("");
        out("✦ 随机事件：" + event.getTitle());
        out("  " + event.getText());
        String effect = event.apply(player);
        if (!effect.isEmpty()) {
            out("  " + effect.replace("\n", "\n  "));
        }
        if (player.hasJustCollapsed()) {
            look();
        }
    }

    // ==================== 时间 ====================

    /**
     * 消耗一次行动并推进时间。
     * 行动次数用尽时强制睡觉，保证游戏不会卡死。
     */
    private boolean spendTurn(int minutes) {
        if (player.getActionsLeft() <= 0) {
            out("你已经累得抬不起手，什么也做不了了。");
            sleep();
            return false;
        }
        player.spendAction();
        advanceTime(minutes);
        return true;
    }

    private void advanceTime(int minutes) {
        clock += minutes;
        if (clock >= 24 * 60) {
            sleep();
        }
    }

    private void sleep() {
        out("");
        out("🌙 24:00，宿舍熄灯。你倒头就睡。");
        day++;
        clock = 6 * 60;
        out(player.startNewDay());
        out("现在是第 " + day + " 天 06:00。");
    }

    // ==================== NPC 对话 ====================

    public void talk(String name) {
        Room room = player.getCurrentRoom();
        if (name == null || name.trim().isEmpty()) {
            out("你想和谁说话？先看看这里有什么人（look）。");
            return;
        }
        NPC npc = CampusData.findNpc(room.getId(), name.trim());
        if (npc == null) {
            out("「" + name.trim() + "」不在这里。");
            return;
        }
        if (!spendTurn(TALK_MINUTES)) {
            return;
        }
        out("");
        out(npc.talk(player));
    }

    // ==================== 搜索（寻宝） ====================

    public void search() {
        if (!spendTurn(SEARCH_MINUTES)) {
            return;
        }
        Room room = player.getCurrentRoom();
        out("");
        out("你在「" + room.getName() + "」里仔细翻找……");

        String foundId = null;
        // 第一次搜索：优先给"保底物品"
        if (room.getGuaranteedItemId() != null && !room.isGuaranteedTaken()) {
            foundId = room.getGuaranteedItemId();
            room.markGuaranteedTaken();
        } else {
            // 之后按 60% 概率从该场景的战利品数组里随机抽一件
            String[] loot = room.getLootIds();
            if (loot.length > 0 && random.nextInt(100) < 60) {
                foundId = loot[random.nextInt(loot.length)];
            }
        }

        if (foundId == null) {
            out("除了灰尘和几张过期传单，什么也没找到。");
            return;
        }
        Item item = CampusData.itemById(foundId);
        if (item == null) {
            out("你摸到一个奇怪的东西，但它似乎不属于这个校园。");
            return;
        }
        if (player.getBag().add(item)) {
            out("你找到了「" + item.getName() + "」！" + item.getDescription());
            if (item.getType() == Item.Type.STAMP) {
                out("【印章】当前 " + player.getStampCount() + "/" + CampusData.TOTAL_STAMPS
                        + "，集齐 6 枚就能开启图书馆顶层的门禁。");
            }
        } else {
            out("你找到了「" + item.getName() + "」，可惜背包已经满了。");
        }
    }

    // ==================== 谜题 ====================

    public void quiz() {
        if (!spendTurn(QUIZ_MINUTES)) {
            return;
        }
        Question question = pickUnsolvedQuestion();
        if (question == null) {
            out("题库已经被你清空了，整个校园的谜题都难不倒你。");
            return;
        }
        activeQuestion = question;
        out("");
        out("【谜题】" + question.getText());
        out("（输入 answer 你的答案 作答，输入 hint 看提示）");
    }

    /** 任务 5 的 Random 应用之二：从还没答对的题目里随机抽一道 */
    private Question pickUnsolvedQuestion() {
        int unsolved = 0;
        for (int i = 0; i < CampusData.QUESTIONS.length; i++) {
            if (!CampusData.QUESTIONS[i].isSolved()) {
                unsolved++;
            }
        }
        if (unsolved == 0) {
            return null;
        }
        int target = random.nextInt(unsolved);
        int seen = 0;
        for (int i = 0; i < CampusData.QUESTIONS.length; i++) {
            Question question = CampusData.QUESTIONS[i];
            if (question.isSolved()) {
                continue;
            }
            if (seen == target) {
                return question;
            }
            seen++;
        }
        return null;
    }

    public void answer(String text) {
        if (activeQuestion == null) {
            out("现在没有待答的题目，先输入 quiz 抽一道。");
            return;
        }
        if (activeQuestion.isSolved()) {
            out("这道题已经答过了，输入 quiz 换一道吧。");
            activeQuestion = null;
            return;
        }
        if (text == null || text.trim().isEmpty()) {
            out("答案不能为空。用法：answer 刘湛恩");
            return;
        }
        advanceTime(ANSWER_MINUTES);
        if (activeQuestion.check(text)) {
            activeQuestion.markSolved();
            player.addSolvedCount();
            out("✔ 回答正确！这道题的答案是「" + text.trim() + "」。");
            out(player.addCredit(activeQuestion.getCreditReward()));
            out("（已答对 " + player.getSolvedCount() + " 题，可以去找社团学长/图书馆管理员交任务）");
            activeQuestion = null;
        } else {
            out("✘ 不太对。再想想，或者输入 hint 看看提示。");
        }
    }

    public void hint() {
        if (activeQuestion == null) {
            out("现在没有待答的题目，先输入 quiz 抽一道。");
            return;
        }
        out("提示：" + activeQuestion.getHint());
    }

    // ==================== 刷题 / 休息 ====================

    public void study() {
        int roomId = player.getCurrentRoom().getId();
        boolean canStudy = roomId == CampusData.ROOM_LAB
                || roomId == CampusData.ROOM_GEZHI
                || roomId == CampusData.ROOM_LIBRARY
                || roomId == CampusData.ROOM_YAN;
        if (!canStudy) {
            out("这里太吵了，学不进去。去思晏堂、格致堂、实验室或图书馆吧。");
            return;
        }
        if (!spendTurn(STUDY_MINUTES)) {
            return;
        }
        out("");
        out("你摊开书，开始刷题。空调嗡嗡作响，时间过得飞快……");

        // 这里就是任务 2 要求的联动触发点：
        // 体力被扣到 0 时，consumeEnergy 内部会自动把玩家送回宿舍并扣行动次数。
        String energyText = player.consumeEnergy(STUDY_ENERGY_COST);
        out(energyText);
        if (player.hasJustCollapsed()) {
            out("（熬夜刷题把体力刷到了 0，你被强制送回了宿舍。）");
            look();
            return;
        }
        int gain = 2 + random.nextInt(3);   // 2~4 点学分
        out("你弄懂了几道题。" + player.addCredit(gain));
    }

    public void rest() {
        Room room = player.getCurrentRoom();
        if (!room.isCanRest()) {
            out("这里没法休息。回宿舍、食堂或校医院吧。");
            return;
        }
        if (!spendTurn(REST_MINUTES)) {
            return;
        }
        out(player.restoreEnergy(REST_ENERGY_GAIN));
        out("你靠着椅背眯了一会儿，醒来精神好多了。");
    }

    // ==================== 战斗 ====================

    public void fight() {
        Monster monster = CampusData.monsterInRoom(player.getCurrentRoom().getId());
        if (monster == null) {
            out("这里没有可以挑战的对手。");
            return;
        }
        if (monster.isBoss() && player.getStampCount() < monster.getNeedStamps()) {
            out("「" + monster.getName() + "」冷笑：印章不够，连见我的资格都没有。");
            out("（需要 " + monster.getNeedStamps() + " 枚印章，你持有 "
                    + player.getStampCount() + " 枚）");
            return;
        }
        battle = new Battle(player, monster, random);
        out("");
        out(battle.start());
    }

    public void attack() {
        if (battle == null) {
            out("现在没有在战斗。用 fight 挑战对手。");
            return;
        }
        out("");
        out(battle.playerAttack());
        if (battle.isOver()) {
            finishBattle();
        }
    }

    public void defend() {
        if (battle == null) {
            out("现在没有在战斗。用 fight 挑战对手。");
            return;
        }
        out("");
        out(battle.playerDefend());
        if (battle.isOver()) {
            finishBattle();
        }
    }

    public void flee() {
        if (battle == null) {
            out("现在没有在战斗。用 fight 挑战对手。");
            return;
        }
        out("");
        out(battle.playerFlee());
        if (battle.isOver()) {
            finishBattle();
        }
    }

    private void finishBattle() {
        Monster monster = battle.getMonster();
        boolean won = battle.isWon();
        boolean escaped = battle.isEscaped();

        if (won) {
            monster.setDefeated(true);
            player.addKillCount();
        }
        out("");
        if (won) {
            out("【战斗结束】你赢了这场对决。");
        } else if (escaped) {
            out("【战斗结束】你退出了战斗，对手还在原地。");
        } else {
            out("【战斗结束】你输了。对手恢复了状态，你也回到了宿舍。");
        }
        battle = null;

        if (won && monster.isBoss()) {
            ending();
            return;
        }
        if (!won && !escaped) {
            look();
        }
    }

    // ==================== 物品 ====================

    public void use(String name) {
        if (name == null || name.trim().isEmpty()) {
            out("你要用什么？用法：use 肉包");
            return;
        }
        Item item = player.getBag().findByKeyword(name.trim());
        if (item == null) {
            out("背包里没有「" + name.trim() + "」。");
            return;
        }
        int before = player.getBag().countOf(item.getId());
        String message = player.useItem(item);
        out(message);
        boolean consumed = player.getBag().countOf(item.getId()) < before;

        // 战斗中吃药会浪费一个回合
        if (consumed && isInBattle()) {
            out("");
            out(battle.playerUsedItem());
            if (battle.isOver()) {
                finishBattle();
            }
        }
    }

    // ==================== 结束 ====================

    public void quit() {
        out("");
        out("你把校园卡塞回口袋，转身走出密氏校门。");
        running = false;
    }

    private void ending() {
        out("");
        out("============================================================");
        out("                    结  局 · 沪 江 迷 踪");
        out("============================================================");
        out("服务器的嗡鸣声一点点低下去，红色的指示灯全部转为绿色。");
        out("屏幕上最后滚过一行字：「选课系统已恢复正常，祝你抢到心仪的课。」");
        out("");
        out("你站在图书馆顶层，手里攥着八枚印章。窗外是湛恩大道，");
        out("梧桐叶落下来，盖住了 1916 年的石阶。");
        out("");
        out("保安大叔的声音从楼下传来：「小伙子，熄灯了——」");
        out("");
        out("【通关统计】");
        out("  姓名：" + player.getName() + "    等级：" + player.getLevel());
        out("  学分：" + player.getCredit() + "/100    体力：" + player.getEnergy() + "/100");
        out("  答对题目：" + player.getSolvedCount() + " / " + CampusData.QUESTIONS.length);
        out("  击败对手：" + player.getKillCount() + " / " + CampusData.MONSTERS.length);
        out("  印章：" + player.getStampCount() + " / " + CampusData.TOTAL_STAMPS);
        out("  用时：第 " + day + " 天 " + clockText());
        out("============================================================");
        running = false;
    }
}
