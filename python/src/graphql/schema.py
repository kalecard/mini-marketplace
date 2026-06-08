import strawberry

from src.graphql.types import (
    Campaign,
    CreateCampaignInput,
    Submission,
    SubmitContentInput,
)


@strawberry.type
class Query:
    @strawberry.field
    async def campaigns(self, info: strawberry.Info) -> list[Campaign]:
        service = info.context["campaign_service"]
        return [Campaign.from_model(c) for c in await service.get_campaigns()]

    @strawberry.field
    async def campaign(self, info: strawberry.Info, id: strawberry.ID) -> Campaign | None:
        model = await info.context["campaign_service"].get_campaign(int(id))
        return Campaign.from_model(model) if model else None

    @strawberry.field
    async def submissions(
        self, info: strawberry.Info, campaign_id: strawberry.ID
    ) -> list[Submission]:
        service = info.context["submission_service"]
        models = await service.get_submissions_by_campaign(int(campaign_id))
        return [Submission.from_model(s) for s in models]


@strawberry.type
class Mutation:
    @strawberry.mutation
    async def create_campaign(
        self, info: strawberry.Info, input: CreateCampaignInput
    ) -> Campaign:
        service = info.context["campaign_service"]
        model = await service.create_campaign(
            int(input.brand_id),
            input.title,
            input.description,
            input.payout_cents,
            input.max_submissions if input.max_submissions is not None else 100,
        )
        return Campaign.from_model(model)

    @strawberry.mutation
    async def activate_campaign(
        self, info: strawberry.Info, campaign_id: strawberry.ID
    ) -> Campaign:
        model = await info.context["campaign_service"].activate_campaign(int(campaign_id))
        return Campaign.from_model(model)

    @strawberry.mutation
    async def submit_content(
        self, info: strawberry.Info, input: SubmitContentInput
    ) -> Submission:
        service = info.context["submission_service"]
        model = await service.submit_content(
            int(input.campaign_id), str(input.creator_id), input.content_url
        )
        return Submission.from_model(model)

    @strawberry.mutation
    async def approve_submission(
        self, info: strawberry.Info, submission_id: strawberry.ID
    ) -> Submission:
        model = await info.context["submission_service"].approve_submission(int(submission_id))
        return Submission.from_model(model)

    @strawberry.mutation
    async def reject_submission(
        self, info: strawberry.Info, submission_id: strawberry.ID
    ) -> Submission:
        model = await info.context["submission_service"].reject_submission(int(submission_id))
        return Submission.from_model(model)


schema = strawberry.Schema(query=Query, mutation=Mutation)
