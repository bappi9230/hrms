package hrms

/** Day 7: অনবোর্ডিং চেকলিস্টের টেমপ্লেট - নতুন কর্মীর জন্য এই কাজগুলো আপনা-আপনি তৈরি হয় (Settings থেকে বদলানো যায়) */
class OnboardingTemplate {

    static final List<String> CATEGORIES = ['DOCUMENTS', 'IT', 'ADMIN', 'HR', 'TRAINING']
    static final List<String> OWNERS = ['HR', 'IT', 'ADMIN', 'ACCOUNTS', 'MANAGER']

    String title
    String category = 'HR'
    String ownerRole = 'HR'
    Integer dueAfterDays = 0               // যোগদানের কত দিনের মধ্যে
    Integer sortOrder = 10
    boolean active = true

    static constraints = {
        title blank: false, maxSize: 150
        category inList: CATEGORIES
        ownerRole inList: OWNERS
        dueAfterDays nullable: false, min: 0, max: 365
        sortOrder nullable: false, min: 0
    }

    static mapping = {
        table 'hrms_onboarding_template'
    }

    String toString() { title }
}
