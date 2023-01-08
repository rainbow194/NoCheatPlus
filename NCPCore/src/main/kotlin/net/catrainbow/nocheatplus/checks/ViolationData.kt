/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in thCut even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.catrainbow.nocheatplus.checks

import cn.nukkit.Player
import net.catrainbow.nocheatplus.actions.ActionFactory
import net.catrainbow.nocheatplus.actions.ActionHistory
import net.catrainbow.nocheatplus.actions.ActionProcess
import net.catrainbow.nocheatplus.actions.ActionType

/**
 * Violation Level 数据
 *
 * @author Catrainbow
 */
class ViolationData(type: CheckType, private val player: Player) {

    private val checkType = type

    private var vl = 0.0

    private var willCancel = false

    private var addedVL = 0.0

    private var multiply = 1.0

    private var actions: ArrayList<ActionProcess> = ArrayList()

    private var history: ActionHistory = ActionHistory()

    fun getVL(): Double {
        return this.vl
    }

    private fun clearBuffer() {
        this.addedVL = 0.0
        this.multiply = 0.0
    }

    fun addVL(vl: Double) {
        this.addedVL += vl
    }

    fun clear() {
        this.willCancel = false
        this.addedVL = 0.0
    }

    fun preVL(buffer: Double) {
        this.multiply = buffer
    }

    fun update() {
        if (!this.willCancel) {
            this.vl += this.addedVL
        } else {
            this.vl *= this.multiply
        }
        this.clearBuffer()
        this.executeAction()
    }

    fun setLagBack() {
        val action = ActionFactory(player, this, ActionType.SETBACK).build()
        this.actions.add(action)
    }

    fun getHistory(): ActionHistory {
        return this.history
    }

    private fun executeAction() {
        val data = ActionFactory.actionDataMap[this.checkType.name]
        if (data!!.enableLog) if (this.history.canLog()) this.actions.add(
            ActionFactory(
                player, this, ActionType.LOG
            ).build()
        )
        if (data.enableWarn) if (this.history.canWarn()) this.actions.add(
            ActionFactory(
                player, this, ActionType.WARING
            ).build()
        )
        if (data.enableKick) if (this.vl > data.kick) this.actions.add(
            ActionFactory(
                player, this, ActionType.KICK
            ).build()
        )
        if (actions.isEmpty()) return
        val action = actions[0]
        val checkType = action.getCheckType()
        val actionData = ActionFactory.actionDataMap[checkType.name]!!
        action.doAction(actionData)
    }

    fun getCheckType(): CheckType {
        return this.checkType
    }

}