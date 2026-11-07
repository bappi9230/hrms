package hrms

import grails.plugin.springsecurity.annotation.Secured

/** Day 9: অনুমোদন ইনবক্স - সব মডিউলের আমার-সিদ্ধান্ত-বাকি কাজ এক জায়গায় */
@Secured(['isAuthenticated()'])
class InboxController {

    def approvalService

    def index() {
        List<Map> items = approvalService.pendingItems()
        [items: items, groups: items.groupBy { it.group }, total: items.size()]
    }
}
