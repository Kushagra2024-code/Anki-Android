// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.reviewer

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ichi2.anki.RobolectricTest
import com.ichi2.anki.reviewer.AutomaticAnswerAction.Companion.answerAction
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
class AutomaticAnswerAndroidTest : RobolectricTest() {
    @Test
    fun default_is_bury() {
        assertThat("no value", createInstance().settings.answerAction, equalTo(AutomaticAnswerAction.BURY_CARD))
        assertThat("default", AutomaticAnswer.defaultInstance(mock()).settings.answerAction, equalTo(AutomaticAnswerAction.BURY_CARD))
    }

    @Test
    fun preference_sets_action() {
        setActionType(AutomaticAnswerAction.ANSWER_AGAIN)
        assertThat(createInstance().settings.answerAction, equalTo(AutomaticAnswerAction.ANSWER_AGAIN))
        // reset the value
        resetPrefs()
        assertThat("default", createInstance().settings.answerAction, equalTo(AutomaticAnswerAction.BURY_CARD))
    }

    @Test
    fun `milliseconds are handled`() {
        setShowQuestionDuration(1.5)
        assertThat(createInstance().settings.millisecondsToShowQuestionFor, equalTo(1500))
    }

    @Test
    fun `queryOptions correctly maps question, answer, and action from deck config`() {
        val conf =
            col.decks.configDictForDeckId(col.decks.selected()).apply {
                answerAction = AutomaticAnswerAction.ANSWER_HARD
                secondsToShowQuestion = 2.5
                secondsToShowAnswer = 4.0
            }
        col.decks.save(conf)

        val settings = AutomaticAnswerSettings.queryOptions(col, col.decks.selected())
        assertThat(settings.answerAction, equalTo(AutomaticAnswerAction.ANSWER_HARD))
        assertThat(settings.millisecondsToShowQuestionFor, equalTo(2500L))
        assertThat(settings.millisecondsToShowAnswerFor, equalTo(4000L))
        assertThat(settings.autoAdvanceIfShowingQuestion, equalTo(true))
        assertThat(settings.autoAdvanceIfShowingAnswer, equalTo(true))
        assertThat(settings.isUsable, equalTo(true))
    }

    @Test
    fun `queryOptions handles zero durations as disabled auto advance`() {
        val conf =
            col.decks.configDictForDeckId(col.decks.selected()).apply {
                secondsToShowQuestion = 0.0
                secondsToShowAnswer = 0.0
            }
        col.decks.save(conf)

        val settings = AutomaticAnswerSettings.queryOptions(col, col.decks.selected())
        assertThat(settings.millisecondsToShowQuestionFor, equalTo(0L))
        assertThat(settings.millisecondsToShowAnswerFor, equalTo(0L))
        assertThat(settings.autoAdvanceIfShowingQuestion, equalTo(false))
        assertThat(settings.autoAdvanceIfShowingAnswer, equalTo(false))
        assertThat(settings.isUsable, equalTo(false))
    }

    private fun resetPrefs() {
        val conf =
            col.decks.configDictForDeckId(col.decks.selected()).apply {
                removeAnswerAction()
            }
        col.decks.save(conf)
    }

    @Suppress("SameParameterValue")
    private fun setActionType(value: AutomaticAnswerAction) {
        val conf =
            col.decks.configDictForDeckId(col.decks.selected()).apply {
                answerAction = value
            }
        col.decks.save(conf)
    }

    @Suppress("SameParameterValue")
    private fun setShowQuestionDuration(value: Double) {
        val conf =
            col.decks.configDictForDeckId(col.decks.selected()).apply {
                secondsToShowQuestion = value
            }
        col.decks.save(conf)
    }

    private fun createInstance() = AutomaticAnswer.createInstance(mock(), super.col)
}
