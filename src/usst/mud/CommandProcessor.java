package usst.mud;

/**
 * 指令解析器：把"一行原始输入"翻译成对 Game 的调用。
 *
 * 任务 4 要求有独立的命令处理方法 processCommand，
 * 所以 main / 游戏循环里没有任何 if-else 判断指令的逻辑，
 * 全部集中在这里，便于单独测试和扩展新指令。
 */
public final class CommandProcessor {

    private CommandProcessor() {
    }

    /**
     * 处理一行输入。
     *
     * 解析步骤：
     * 1. trim() 去掉首尾空白（包括用户误按的空格）；
     * 2. 空输入单独提示，而不是抛异常；
     * 3. split("\\s+", 2) 只切两段：指令 + 其余全部参数，
     *    这样 answer 的答案里带空格也不会被切碎；
     * 4. 指令统一 toLowerCase()，实现大小写不敏感；
     * 5. 根据当前是否在战斗中，分派到两套指令表。
     */
    public static void processCommand(String raw, Game game) {
        if (game == null) {
            throw new IllegalArgumentException("游戏实例不能为空");
        }
        // 兼容从文本文件批量输入时可能带上的 BOM 字符
        String input = raw == null ? "" : raw.replace("\uFEFF", "").trim();
        if (input.isEmpty()) {
            game.out("（你张了张嘴，什么也没说。输入 help 看看能做什么。）");
            return;
        }

        String[] parts = input.split("\\s+", 2);
        String head = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1].trim() : "";

        if (game.isInBattle()) {
            handleBattleCommand(head, arg, game);
            return;
        }

        // 允许直接输入方向，例如 north / 北，等价于 go north
        if (Room.directionIndex(head) >= 0) {
            game.move(head);
            return;
        }

        switch (head) {
            case "help":
            case "?":
            case "h":
            case "帮助":
            case "指令":
                game.help();
                return;

            case "look":
            case "l":
            case "看":
            case "观察":
                game.look();
                return;

            case "go":
            case "走":
            case "去":
                if (arg.isEmpty()) {
                    game.out("你要往哪个方向走？用法：go north / go south / go 北 ……");
                    return;
                }
                game.move(arg);
                return;

            case "status":
            case "t":
            case "状态":
                game.status();
                return;

            case "map":
            case "地图":
                game.showMap();
                return;

            case "bag":
            case "inv":
            case "i":
            case "背包":
                game.showBag();
                return;

            case "talk":
            case "说":
            case "对话":
                game.talk(arg);
                return;

            case "search":
            case "搜":
            case "搜索":
                game.search();
                return;

            case "quiz":
            case "答题":
            case "题目":
                game.quiz();
                return;

            case "answer":
            case "答":
            case "回答":
                game.answer(arg);
                return;

            case "hint":
            case "提示":
                game.hint();
                return;

            case "study":
            case "自习":
            case "刷题":
                game.study();
                return;

            case "rest":
            case "休息":
                game.rest();
                return;

            case "fight":
            case "挑战":
            case "战斗":
                game.fight();
                return;

            case "attack":
            case "攻击":
            case "打":
                game.attack();
                return;

            case "defend":
            case "防御":
            case "守":
                game.defend();
                return;

            case "flee":
            case "逃跑":
            case "跑":
                game.flee();
                return;

            case "use":
            case "用":
            case "使用":
                game.use(arg);
                return;

            case "quit":
            case "exit":
            case "q":
            case "退出":
                game.quit();
                return;

            default:
                game.out("「" + parts[0] + "」不是一个能听懂的指令。输入 help 查看指令列表。");
        }
    }

    /** 战斗中只开放少量指令，避免玩家用 look 之类"跳过回合" */
    private static void handleBattleCommand(String head, String arg, Game game) {
        switch (head) {
            case "attack":
            case "a":
            case "攻击":
            case "打":
                game.attack();
                return;
            case "defend":
            case "防御":
            case "守":
                game.defend();
                return;
            case "flee":
            case "f":
            case "逃跑":
            case "跑":
                game.flee();
                return;
            case "use":
            case "用":
            case "使用":
                game.use(arg);
                return;
            case "status":
            case "t":
            case "状态":
                game.status();
                return;
            case "help":
            case "?":
            case "帮助":
                game.help();
                return;
            case "quit":
            case "q":
            case "退出":
                game.quit();
                return;
            default:
                game.out("战斗中只能使用 attack（攻击）/ defend（防御）/ flee（逃跑）/ use 物品。");
        }
    }
}
