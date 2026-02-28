# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**
```txt

```
Open api gives proper standards for all the api's. It also helps in working parallely with front end team as the api contract is finalized in design phase. It also adds a overhead if any api contract changes required.

Endpoint handlers are more dynamic and flexible for api development as we can control all the aspects. Any changes in the api contract can be easily accomodated.

I would choose open api approach as that will give proper standards for all the api end points.
---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```
As this is a fulfillment application there would be more users accessing the application at the same time. And our focus should be on multi threading and locking technologies with the DB. There sould not be any issues to data integrity. So we should focus on integration tests. Unit testing should be focused more on Repository layer on CURD operation. Service layer should be tested for business logic. 

```
