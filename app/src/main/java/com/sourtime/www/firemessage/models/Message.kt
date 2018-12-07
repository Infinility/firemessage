package com.sourtime.www.firemessage.models


class Message(val fromid: String, val toid: String, val message: String, val timestamp: Long?) {
    constructor() : this("","","",-1)
}