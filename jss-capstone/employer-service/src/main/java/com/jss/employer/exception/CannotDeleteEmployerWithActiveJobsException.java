package com.jss.employer.exception;

/**
 * Architectural Template Exception:
 * Thrown when an attempt is made to delete an employer organization that still has active
 * job postings published on the Job Search System platform.
 *
 * Status Code: HTTP 409 (Conflict).
 *
 * NOTE FOR ARCHITECTURAL ROADMAP:
 * This exception is currently unwired in Step 2A. It will be referenced and wired in Step 3
 * once 'job-service' and its OpenFeign declarative client ('JobServiceClient') are established.
 * In Step 3, the EmployerServiceImpl.deleteEmployer(Long id) method will query job-service
 * to ensure countOfActiveJobs == 0 before performing physical deletion.
 */
public class CannotDeleteEmployerWithActiveJobsException extends RuntimeException {

    public CannotDeleteEmployerWithActiveJobsException(String message) {
        super(message);
    }

    public CannotDeleteEmployerWithActiveJobsException(Long employerId, long activeJobsCount) {
        super(String.format("Cannot delete employer ID %d: There are still %d active job postings associated with this employer. Please close or delete all active jobs first.", employerId, activeJobsCount));
    }
}
