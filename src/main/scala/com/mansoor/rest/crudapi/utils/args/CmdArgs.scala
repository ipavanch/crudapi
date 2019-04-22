package com.mansoor.rest.crudapi.utils.args

import java.net.InetAddress

case class CmdArgs(bindInterface: InetAddress = InetAddress.getByName("localhost"),
                   port: Int = 0,
                   checkDBConnection: Boolean = false,
                   start: Boolean = false)