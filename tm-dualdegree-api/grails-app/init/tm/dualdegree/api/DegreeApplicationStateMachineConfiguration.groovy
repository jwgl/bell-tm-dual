package tm.dualdegree.api

import cn.edu.bnuz.bell.workflow.Activities
import cn.edu.bnuz.bell.workflow.Event
import cn.edu.bnuz.bell.workflow.State
import cn.edu.bnuz.bell.workflow.actions.AbstractEntryAction
import cn.edu.bnuz.bell.workflow.actions.AutoEntryAction
import cn.edu.bnuz.bell.workflow.actions.ManualEntryAction
import cn.edu.bnuz.bell.workflow.actions.SubmittedEntryAction
import cn.edu.bnuz.bell.workflow.config.StandardActionConfiguration
import cn.edu.bnuz.bell.workflow.events.EventData
import cn.edu.bnuz.bell.workflow.events.ManualEventData
import cn.edu.bnuz.bell.workflow.events.RejectEventData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.statemachine.StateContext
import org.springframework.statemachine.action.Action
import org.springframework.statemachine.config.EnableStateMachine
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer

@Configuration
@EnableStateMachine(name='DegreeApplicationStateMachine')
@Import(StandardActionConfiguration)
class DegreeApplicationStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<State, Event> {
    @Autowired
    StandardActionConfiguration actions

    @Override
    void configure(StateMachineStateConfigurer<State, Event> states) throws Exception {
        states
                .withStates()
                .initial(State.CREATED)
                .state(State.CREATED,   [actions.logEntryAction()], null)
                .state(State.STEP1,     [actions.logEntryAction(), submittedEntryAction()], [actions.workitemProcessedAction()])
                .state(State.STEP2,     [actions.logEntryAction(), approvedEntryAction()], [actions.workitemProcessedAction()])
                .state(State.STEP3,     [actions.logEntryAction(), progressEntryAction()], [actions.workitemProcessedAction()])
                .state(State.STEP4,     [actions.logEntryAction(), finishEntryAction()], [actions.workitemProcessedAction()])
                .state(State.STEP5,     [actions.logEntryAction(), actions.rejectedEntryAction()], [actions.workitemProcessedAction()])
                .state(State.REJECTED,  [actions.logEntryAction(), actions.rejectedEntryAction()],  [actions.workitemProcessedAction()])
                .state(State.FINISHED,  [actions.logEntryAction(), actions.notifySubmitterAction()], null)
    }

    @Override
    void configure(StateMachineTransitionConfigurer<State, Event> transitions) throws Exception {
        transitions
                .withInternal()
                .source(State.CREATED)
                .event(Event.UPDATE)
                .action(actions.logTransitionAction())
                .and()
                .withExternal()
                .source(State.CREATED)
                .event(Event.SUBMIT)
                .target(State.STEP1)
                .and()
                .withExternal()
                .source(State.STEP1)
                .event(Event.NEXT)
                .target(State.STEP2)
                .and()
                .withExternal()
                .source(State.STEP1)
                .event(Event.REJECT)
                .target(State.REJECTED)
                .and()
                .withInternal()
                .source(State.REJECTED)
                .event(Event.UPDATE)
                .action(actions.logTransitionAction())
                .and()
                .withExternal()
                .source(State.STEP2)
                .event(Event.NEXT)
                .target(State.STEP3)
                .and()
                .withExternal()
                .source(State.STEP3)
                .event(Event.NEXT)
                .target(State.STEP4)
                .and()
                .withExternal()
                .source(State.STEP3)
                .event(Event.FINISH)
                .target(State.FINISHED)
                .and()
                .withExternal()
                .source(State.STEP4)
                .event(Event.FINISH)
                .target(State.FINISHED)
                .and()
                .withExternal()
                .source(State.STEP4)
                .event(Event.REJECT)
                .target(State.STEP5)
                .and()
                .withExternal()
                .source(State.STEP3)
                .event(Event.REJECT)
                .target(State.STEP2)
                .and()
                .withInternal()
                .source(State.STEP2)
                .event(Event.UPDATE)
                .action(actions.logTransitionAction())
                .and()
                .withExternal()
                .source(State.REJECTED)
                .event(Event.SUBMIT)
                .target(State.STEP1)
                .and()
                .withExternal()
                .source(State.STEP5)
                .event(Event.NEXT)
                .target(State.STEP4)
    }

    @Bean
    Action<State, Event> submittedEntryAction() {
        new SubmittedEntryAction(Activities.CHECK)
    }

    @Bean
    Action<State, Event> approvedEntryAction() {
        new AbstractEntryAction() {
            @Override
            void execute(StateContext<State, Event> context) {
                def data = context.getMessageHeader(EventData.KEY)
                if (data instanceof ManualEventData ) {
                    def event = data as ManualEventData
                    workflowService.createWorkitem(data.entity.workflowInstance,
                            event.fromUser,
                            context.event,
                            context.target.id,
                            event.comment,
                            event.ipAddress,
                            event.toUser,
                            'submitPaper',
                    )

                } else if (data instanceof RejectEventData) {
                    def event = data as RejectEventData
                    workflowService.createWorkitem(
                            event.entity.workflowInstance,
                            event.fromUser,
                            context.event,
                            context.target.id,
                            event.comment,
                            event.ipAddress,
                            Activities.VIEW,
                    )
                } else {
                    throw new Exception('Unsupported event type')
                }
            }
        }
    }

    @Bean
    Action<State, Event> progressEntryAction() {
        new ManualEntryAction('checkPaper')
    }

    @Bean
    Action<State, Event> finishEntryAction() {
        new ManualEntryAction('approvePaper')
    }
}
