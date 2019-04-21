package com.mansoor.rest.crudapi.utils.args

import java.net.InetAddress

case class CmdArgs(bindInterface: InetAddress = InetAddress.getByName("127.0.0.1"), port: Int = 0, start: Boolean = false)