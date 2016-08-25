package wordpackbot.bots

import groovy.transform.Immutable

@Immutable
class UpdateEvent {
    String text
    Long userId
    Long chatId
}
