import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';

import { IPost, Post } from 'app/shared/model/post.model';
import { PostService } from './post.service';
import { IUser } from 'app/core/user/user.model';
import { UserService } from 'app/core/user/user.service';
import { ITopic } from 'app/shared/model/topic.model';
import { TopicService } from 'app/entities/topic/topic.service';

type SelectableEntity = IUser | ITopic;

@Component({
  selector: 'jhi-post-update',
  templateUrl: './post-update.component.html',
})
export class PostUpdateComponent implements OnInit {
  isSaving = false;
  users: IUser[] = [];
  topics: ITopic[] = [];

  editForm = this.fb.group({
    id: [],
    title: [null, [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    content: [null, [Validators.required, Validators.minLength(2), Validators.maxLength(4096)]],
    date: [null, [Validators.required]],
    rankOne: [],
    rankTwo: [],
    rankThree: [],
    rankFour: [],
    rankFive: [],
    rankSix: [],
    rankSeven: [],
    user: [null, Validators.required],
    topic: [null, Validators.required],
  });

  constructor(
    protected postService: PostService,
    protected userService: UserService,
    protected topicService: TopicService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ post }) => {
      if (!post.id) {
        const today = moment().startOf('day');
        post.date = today;
      }

      this.updateForm(post);

      this.userService.query().subscribe((res: HttpResponse<IUser[]>) => (this.users = res.body || []));

      this.topicService.query().subscribe((res: HttpResponse<ITopic[]>) => (this.topics = res.body || []));
    });
  }

  updateForm(post: IPost): void {
    this.editForm.patchValue({
      id: post.id,
      title: post.title,
      content: post.content,
      date: post.date ? post.date.format(DATE_TIME_FORMAT) : null,
      rankOne: post.rankOne,
      rankTwo: post.rankTwo,
      rankThree: post.rankThree,
      rankFour: post.rankFour,
      rankFive: post.rankFive,
      rankSix: post.rankSix,
      rankSeven: post.rankSeven,
      user: post.user,
      topic: post.topic,
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const post = this.createFromForm();
    if (post.id !== undefined) {
      this.subscribeToSaveResponse(this.postService.update(post));
    } else {
      this.subscribeToSaveResponse(this.postService.create(post));
    }
  }

  private createFromForm(): IPost {
    return {
      ...new Post(),
      id: this.editForm.get(['id'])!.value,
      title: this.editForm.get(['title'])!.value,
      content: this.editForm.get(['content'])!.value,
      date: this.editForm.get(['date'])!.value ? moment(this.editForm.get(['date'])!.value, DATE_TIME_FORMAT) : undefined,
      rankOne: this.editForm.get(['rankOne'])!.value,
      rankTwo: this.editForm.get(['rankTwo'])!.value,
      rankThree: this.editForm.get(['rankThree'])!.value,
      rankFour: this.editForm.get(['rankFour'])!.value,
      rankFive: this.editForm.get(['rankFive'])!.value,
      rankSix: this.editForm.get(['rankSix'])!.value,
      rankSeven: this.editForm.get(['rankSeven'])!.value,
      user: this.editForm.get(['user'])!.value,
      topic: this.editForm.get(['topic'])!.value,
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPost>>): void {
    result.subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  protected onSaveSuccess(): void {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError(): void {
    this.isSaving = false;
  }

  trackById(index: number, item: SelectableEntity): any {
    return item.id;
  }
}
