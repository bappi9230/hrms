package hrms

import java.time.LocalDate

/**
 * Day 9: কোম্পানির ঘোষণা। HR/Admin দেয়, সবাই দেখে।
 * চালু থাকার শর্ত: active = true, publishDate আজ বা আগের, expiryDate ফাঁকা বা আজ/ভবিষ্যতের।
 */
class Announcement {

    static final List<String> CATEGORIES = ['GENERAL', 'POLICY', 'HOLIDAY', 'EVENT', 'URGENT']

    String title
    String body
    String category = 'GENERAL'
    Boolean pinned = false                 // সবার ওপরে আটকে রাখা
    Boolean active = true                  // বন্ধ/চালু
    LocalDate publishDate
    LocalDate expiryDate                   // ফাঁকা মানে মেয়াদ নেই
    String postedBy
    Date dateCreated

    static constraints = {
        title blank: false, maxSize: 150
        body blank: false, maxSize: 4000
        category inList: CATEGORIES
        publishDate nullable: false
        expiryDate nullable: true
        postedBy nullable: true, maxSize: 50
    }

    static mapping = {
        table 'hrms_announcement'
        body type: 'text'
    }

    String toString() { title }
}
