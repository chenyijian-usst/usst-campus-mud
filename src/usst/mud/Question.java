package usst.mud;

/**
 * 校园谜题。答案判断刻意使用了 String 的 trim / equalsIgnoreCase / replace，
 * 对应任务 5 对 String 常用方法的要求。
 */
public class Question {

    private final String text;
    private final String[] acceptedAnswers;   // 允许的多种写法
    private final String hint;
    private final int creditReward;
    private boolean solved;

    public Question(String text, String[] acceptedAnswers, String hint, int creditReward) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("题目不能为空");
        }
        if (acceptedAnswers == null || acceptedAnswers.length == 0) {
            throw new IllegalArgumentException("题目至少要有一个标准答案");
        }
        if (creditReward < 0 || creditReward > Player.MAX_CREDIT) {
            throw new IllegalArgumentException("题目奖励学分不合法：" + creditReward);
        }
        this.text = text.trim();
        this.acceptedAnswers = acceptedAnswers.clone();
        this.hint = hint == null ? "（没有提示，靠你自己了）" : hint;
        this.creditReward = creditReward;
        this.solved = false;
    }

    /**
     * 判断玩家输入是否正确：
     * 1. trim() 去掉首尾空格；
     * 2. toLowerCase() 统一大小写；
     * 3. replace(" ", "") 去掉答案中间的空格，兼容"刘 湛 恩"这种输入；
     * 4. 逐个比对标准答案，也允许答案被包含在输入里。
     */
    public boolean check(String input) {
        if (input == null) {
            return false;
        }
        String normalized = normalize(input);
        if (normalized.isEmpty()) {
            return false;
        }
        for (int i = 0; i < acceptedAnswers.length; i++) {
            String answer = normalize(acceptedAnswers[i]);
            if (normalized.equalsIgnoreCase(answer)) {
                return true;
            }
            // 允许"答案是刘湛恩"这类稍微啰嗦的写法，但拒绝把整张答案表一次性粘进来
            if (answer.length() >= 2 && normalized.length() <= answer.length() + 4
                    && normalized.contains(answer)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String s) {
        return s.trim().toLowerCase().replace(" ", "").replace("　", "");
    }

    public String getText() {
        return text;
    }

    public String getHint() {
        return hint;
    }

    public int getCreditReward() {
        return creditReward;
    }

    public boolean isSolved() {
        return solved;
    }

    public void markSolved() {
        this.solved = true;
    }
}
