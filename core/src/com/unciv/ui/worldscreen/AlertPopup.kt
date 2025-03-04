package com.unciv.ui.worldscreen

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.unciv.logic.civilization.AlertType
import com.unciv.logic.civilization.CivilizationInfo
import com.unciv.logic.civilization.PopupAlert
import com.unciv.models.gamebasics.tr
import com.unciv.ui.utils.addSeparator
import com.unciv.ui.utils.onClick
import com.unciv.ui.utils.toLabel
import com.unciv.ui.worldscreen.optionstable.PopupTable

class AlertPopup(val worldScreen: WorldScreen, val popupAlert: PopupAlert): PopupTable(worldScreen){
    fun getCloseButton(text: String, action: (() -> Unit)?=null): TextButton {
        val button = TextButton(text.tr(), skin)
        button.onClick {
            if(action!=null) action()
            worldScreen.shouldUpdate=true
            close()
        }
        return button
    }

    fun addLeaderName(civInfo : CivilizationInfo){
        val otherCivLeaderName = civInfo.getLeaderDisplayName()
        add(otherCivLeaderName.toLabel())
        addSeparator()
    }

    init {

        when(popupAlert.type){
            AlertType.WarDeclaration -> {
                val civInfo = worldScreen.gameInfo.getCivilization(popupAlert.value)
                addLeaderName(civInfo)
                addGoodSizedLabel(civInfo.getTranslatedNation().declaringWar).row()
                val responseTable = Table()
                responseTable.add(getCloseButton("You'll pay for this!"))
                responseTable.add(getCloseButton("Very well."))
                add(responseTable)
            }
            AlertType.Defeated -> {
                val civInfo = worldScreen.gameInfo.getCivilization(popupAlert.value)
                addLeaderName(civInfo)
                addGoodSizedLabel(civInfo.getTranslatedNation().defeated).row()
                add(getCloseButton("Farewell."))
            }
            AlertType.FirstContact -> {
                val civInfo = worldScreen.gameInfo.getCivilization(popupAlert.value)
                val translatedNation = civInfo.getTranslatedNation()
                if (civInfo.isCityState()) {
                    addLeaderName(civInfo)
                    addGoodSizedLabel("We have encountered the City-State of [${translatedNation.getNameTranslation()}]!").row()
                    add(getCloseButton("Excellent!"))
                } else {
                    addLeaderName(civInfo)
                    addGoodSizedLabel(translatedNation.introduction).row()
                    add(getCloseButton("A pleasure to meet you."))
                }
            }
            AlertType.CityConquered -> {
                addGoodSizedLabel("What would you like to do with the city?").row()
                add(getCloseButton("Annex")).row()
                add(TextButton("Raze", skin).onClick {
                    worldScreen.viewingCiv.cities.first { it.name==popupAlert.value }.isBeingRazed=true
                    worldScreen.shouldUpdate=true
                    close()
                })
            }
            AlertType.BorderConflict -> {
                val civInfo = worldScreen.gameInfo.getCivilization(popupAlert.value)
                addLeaderName(civInfo)
                addGoodSizedLabel("Remove your troops in our border immediately!").row()
                val responseTable = Table()
                responseTable.add(getCloseButton("Sorry."))
                responseTable.add(getCloseButton("Never!"))
                add(responseTable)
            }
            AlertType.DemandToStopSettlingCitiesNear -> {
                val otherciv= worldScreen.gameInfo.getCivilization(popupAlert.value)
                val playerDiploManager = worldScreen.viewingCiv.getDiplomacyManager(otherciv)
                addLeaderName(otherciv)
                addGoodSizedLabel("Please don't settle new cities near us.").row()
                add(getCloseButton("Very well, we shall look for new lands to settle."){
                    playerDiploManager.agreeNotToSettleNear()
                }).row()
                add(getCloseButton("We shall do as we please.") {
                    playerDiploManager.refuseDemandNotToSettleNear()
                }).row()
            }
            AlertType.CitySettledNearOtherCivDespiteOurPromise -> {
                val otherciv= worldScreen.gameInfo.getCivilization(popupAlert.value)
                addLeaderName(otherciv)
                addGoodSizedLabel("We noticed your new city near our borders, despite your promise. This will have....implications.").row()
                add(getCloseButton("Very well."))
            }
        }
        open()
        worldScreen.alertPopupIsOpen = true
    }

    override fun close(){
        worldScreen.viewingCiv.popupAlerts.remove(popupAlert)
        worldScreen.alertPopupIsOpen = false
        super.close()
    }
}