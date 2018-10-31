package org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.courses.processors.ProcessorContext;
import org.gooru.nucleus.handlers.courses.processors.repositories.CourseRepo;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.courses.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.courses.processors.responses.MessageResponse;

public class AJCourseRepo implements CourseRepo {
    private final ProcessorContext context;

    public AJCourseRepo(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public MessageResponse fetchCourseCards() {
        return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchCourseCardsHandler(context));
    }

    @Override
    public MessageResponse fetchCourse() {
        return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchCourseHandler(context));
    }

    @Override
    public MessageResponse createCourse() {
        return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildCreateCourseHandler(context));
    }

    @Override
    public MessageResponse updateCourse() {
        return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildUpdateCourseHandler(context));
    }

    @Override
    public MessageResponse deleteCourse() {
        return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildDeleteCourseHandler(context));
    }

    @Override
    public MessageResponse reorderUnitInCourse() {
        return new TransactionExecutor()
            .executeTransaction(new DBHandlerBuilder().buildReorderUnitInCourseHandler(context));
    }

    @Override
    public MessageResponse moveUnitToCourse() {
        return new TransactionExecutor()
            .executeTransaction(new DBHandlerBuilder().buildMoveUnitToCourseHandler(context));
    }

    @Override
    public MessageResponse reorderCourse() {
        return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildReorderCourseHandler(context));
    }

	@Override
	public MessageResponse fetchResourcesForCourse() {
		return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchResourcesForCourse(context));
	}

    @Override
    public MessageResponse fetchAssessmentsByCourse() {
        return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchAssessmentsByCourse(context));
    }

    @Override
    public MessageResponse fetchCollectionsByCourse() {
        return new TransactionExecutor().executeTransaction(new DBHandlerBuilder().buildFetchCollectionsByCourse(context));
    }

}
