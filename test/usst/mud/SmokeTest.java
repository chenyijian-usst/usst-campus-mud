package usst.mud;

import java.util.Random;

/**
 * 自测类：不启动游戏循环，直接验证几条关键业务规则。
 * 运行方式（在项目根目录）：
 *   javac -encoding UTF-8 -d out src/usst/mud/*.java test/usst/mud/SmokeTest.java
 *   java -cp out usst.mud.SmokeTest
 */
public class SmokeTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testEnergyCollapse();
        testCreditClamp();
        testMoveCost();
        testRoomExitData();
        testQuestionCheck();
        testInventoryCapacity();
        testSideQuest();
        testBossBattle();

        System.out.println();
        System.out.println("自测结束：通过 " + passed + " 项，失败 " + failed + " 项。");
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** 体力不能小于 0，且归零时自动回宿舍、扣行动次数 */
    private static void testEnergyCollapse() {
        Player player = new Player("测试员",
                CampusData.ROOMS[CampusData.ROOM_GATE],
                CampusData.ROOMS[CampusData.ROOM_DORM]);
        int actionsBefore = player.getActionsLeft();
        String message = player.consumeEnergy(150);
        check("体力被夹到 0 后触发透支", message.contains("体力透支"));
        check("透支后位置重置为宿舍", player.getCurrentRoom() == CampusData.ROOMS[CampusData.ROOM_DORM]);
        check("透支后体力恢复为 20", player.getEnergy() == 20);
        check("透支后扣 5 次行动", player.getActionsLeft() == actionsBefore - 5);
    }

    /** 学分严格锁定在 0~100 */
    private static void testCreditClamp() {
        Player player = new Player("测试员",
                CampusData.ROOMS[CampusData.ROOM_GATE],
                CampusData.ROOMS[CampusData.ROOM_DORM]);
        player.addCredit(250);
        check("学分上限为 100", player.getCredit() == 100);
        player.addCredit(-500);
        check("学分下限为 0", player.getCredit() == 0);
    }

    /** 正常移动扣 5 点体力 */
    private static void testMoveCost() {
        Player player = new Player("测试员",
                CampusData.ROOMS[CampusData.ROOM_GATE],
                CampusData.ROOMS[CampusData.ROOM_DORM]);
        player.moveTo(CampusData.ROOMS[CampusData.ROOM_ROAD]);
        check("移动后体力为 95", player.getEnergy() == 95);
        check("移动后位置正确", player.getCurrentRoom() == CampusData.ROOMS[CampusData.ROOM_ROAD]);
    }

    /** 所有出口下标都必须落在 ROOMS 数组范围内 */
    private static void testRoomExitData() {
        boolean ok = true;
        for (int i = 0; i < CampusData.ROOMS.length; i++) {
            Room room = CampusData.ROOMS[i];
            for (int dir = 0; dir < Room.DIRECTIONS.length; dir++) {
                int target = room.getExit(dir);
                if (target != -1 && (target < 0 || target >= CampusData.ROOMS.length)) {
                    ok = false;
                    System.out.println("  ! 场景 " + room.getName() + " 的出口越界：" + target);
                }
            }
        }
        check("14 个场景的出口数据全部合法", ok);
        check("场景数组长度为 14", CampusData.ROOMS.length == 14);
        check("对手数组长度为 6", CampusData.MONSTERS.length == 6);
        check("谜题数组长度为 8", CampusData.QUESTIONS.length == 8);
        check("NPC 数组长度为 8", CampusData.NPCS.length == 8);
        check("物品数组长度为 18", CampusData.ITEMS.length == 18);
        check("支线阶段提示共 5 条", CampusData.PHOTO_QUEST_HINTS.length == 5);
    }

    /** 判题要兼容多余空格和大小写 */
    private static void testQuestionCheck() {
        Question q = CampusData.QUESTIONS[0];
        check("判题兼容首尾空格", q.check("  思学志远  "));
        check("判题兼容中间空格", q.check("思 学 志 远"));
        check("判题拒绝错误答案", !q.check("厚德载物"));
    }

    /** 背包容量上限为 16 格 */
    private static void testInventoryCapacity() {
        Inventory inventory = new Inventory();
        boolean allAdded = true;
        for (int i = 0; i < 16; i++) {
            allAdded = allAdded && inventory.add(CampusData.ITEMS[i]);
        }
        check("背包可以放下 16 种物品", allAdded);
        check("背包第 17 格被拒绝", !inventory.add(CampusData.ITEMS[16]));
        check("按类型统计印章数量", inventory.countOfType(Item.Type.STAMP) == 8);
    }

    /** 支线「照片里的 1987」：阶段推进、道具消耗、保安第二段对话 */
    private static void testSideQuest() {
        Player player = new Player("测试员",
                CampusData.ROOMS[CampusData.ROOM_SIPEI],
                CampusData.ROOMS[CampusData.ROOM_DORM]);
        NPC professor = CampusData.findNpc(CampusData.ROOM_SIPEI, "陈教授");
        check("思裴堂能找到陈教授", professor != null);
        if (professor == null) {
            return;
        }

        // 阶段 1：拿到照片
        player.getBag().add(CampusData.itemById("photo"));
        player.setQuestStage(CampusData.PHOTO_QUEST_ID, 1);
        String firstTalk = professor.talk(player);
        check("陈教授认出照片里的人", firstTalk.contains("周建国"));
        check("照片阶段推进到 2", player.getQuestStage(CampusData.PHOTO_QUEST_ID) == 2);
        check("照片不会被收走", player.getBag().has("photo"));

        // 阶段 2：还没有毕业册
        String noYearbook = professor.talk(player);
        check("缺毕业册时给出提示", noYearbook.contains("毕业册"));
        check("缺毕业册时不推进阶段", player.getQuestStage(CampusData.PHOTO_QUEST_ID) == 2);

        // 阶段 2：拿到毕业册并交付
        player.getBag().add(CampusData.itemById("yearbook"));
        professor.talk(player);
        check("毕业册被收走", !player.getBag().has("yearbook"));
        check("真相阶段推进到 3", player.getQuestStage(CampusData.PHOTO_QUEST_ID) == 3);

        // 阶段 3：保安先发印章，再认照片
        Player guardPlayer = new Player("测试员",
                CampusData.ROOMS[CampusData.ROOM_GATE],
                CampusData.ROOMS[CampusData.ROOM_DORM]);
        NPC guard = CampusData.findNpc(CampusData.ROOM_GATE, "保安大叔");
        guard.talk(guardPlayer);
        check("保安的第一段对话仍然发印章", guardPlayer.getBag().has("stamp_gate"));

        guardPlayer.getBag().add(CampusData.itemById("photo"));
        guardPlayer.setQuestStage(CampusData.PHOTO_QUEST_ID, 3);
        String guardTalk = guard.talk(guardPlayer);
        check("保安认出老照片", guardTalk.contains("1987"));
        check("拿到 1987 校徽", guardPlayer.getBag().has("badge_1987"));
        check("支线完成阶段为 4", guardPlayer.getQuestStage(CampusData.PHOTO_QUEST_ID) == 4);
        check("照片被保安收走", !guardPlayer.getBag().has("photo"));
        check("支线奖励学分到账", guardPlayer.getCredit() >= 6);
    }

    /** 集齐 6 枚印章后可以打赢最终 Boss */
    private static void testBossBattle() {
        Player player = new Player("测试员",
                CampusData.ROOMS[CampusData.ROOM_MACHINE],
                CampusData.ROOMS[CampusData.ROOM_DORM]);
        String[] stampIds = {"stamp_gate", "stamp_road", "stamp_yan", "stamp_sipei", "stamp_hall", "stamp_gezhi"};
        for (int i = 0; i < stampIds.length; i++) {
            player.getBag().add(CampusData.itemById(stampIds[i]));
        }
        check("集齐 6 枚印章", player.getStampCount() == 6);

        Monster boss = CampusData.MONSTERS[5];
        Battle battle = new Battle(player, boss, new Random(1));
        battle.start();
        int guard = 0;
        while (!battle.isOver() && guard < 50) {
            if (player.getEnergy() < 30) {
                player.restoreEnergy(60);
            }
            battle.playerAttack();
            guard++;
        }
        check("Boss 战可以正常结束", battle.isOver());
        check("Boss 战可以获胜", battle.isWon());
        if (battle.isWon()) {
            check("获胜后获得最终印章", player.getBag().has("stamp_final"));
            check("获胜后学分增加", player.getCredit() > 0);
        }
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + description);
        } else {
            failed++;
            System.out.println("[FAIL] " + description);
        }
    }
}
