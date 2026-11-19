package com.jss.jobseeker.exception;

/**
 * Architectural Template Exception:
 * Thrown when an attempt is made to delete a candidate profile who still has
 * active
 * job applications in progress (e.g. status is 'Applied', 'Under Review', or
 * 'Shortlisted').
 *
 * Status Code: HTTP 409 (Conflict).
 *
 * NOTE FOR ARCHITECTURAL ROADMAP:
 * This exception is created as an architectural template in Step 2B. It will be
 * referenced
 * and wired in Step 4 once 'application-service' and its OpenFeign declarative
 * client
 * ('ApplicationServiceClient') are implemented. At that time,
 * JobSeekerServiceImpl.deleteJobSeeker()
 * will query application-service to verify active applications == 0 before
 * performing physical deletion.
 */
public class CannotDeleteJobSeekerWithActiveApplicationsException extends RuntimeException {

    public CannotDeleteJobSeekerWithActiveApplicationsException(String message) {
        super(message);
    }

    public CannotDeleteJobSeekerWithActiveApplicationsException(Long jobSeekerId, long activeApplicationsCount) {
        super(String.format(
                "Cannot delete candidate ID %d: There are still %d active job applications associated with this candidate. Please withdraw or resolve all active applications first.",
                jobSeekerId, activeApplicationsCount));
    }
}
