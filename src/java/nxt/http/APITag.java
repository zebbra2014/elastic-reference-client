package nxt.http;

public enum APITag {

    ACCOUNTS("Accounts"), ALIASES("Aliases"), AE("Asset Exchange"), CREATE_TRANSACTION("Create Transaction"),
    BLOCKS("Blocks"), DGS("Digital Goods Store"), FORGING("Forging"), INFO("Server Info"), MESSAGES("Messages"),
    TRANSACTIONS("Transactions"), VS("Voting System"), WC("Work Control"), POX("Proof-of-X"), SEARCH("Search"), UTILS("Utils"), DEBUG("Debug"), CANCEL_TRANSACTION("Cancel Transaction"), BOUNTY("Bounty"), POW("Pow");

    private final String displayName;

    private APITag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
