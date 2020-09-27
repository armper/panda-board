import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { IOverheardComment } from 'app/shared/model/overheard-comment.model';
import { OverheardCommentService } from './overheard-comment.service';
import { OverheardCommentDeleteDialogComponent } from './overheard-comment-delete-dialog.component';

@Component({
  selector: 'jhi-overheard-comment',
  templateUrl: './overheard-comment.component.html',
})
export class OverheardCommentComponent implements OnInit, OnDestroy {
  overheardComments?: IOverheardComment[];
  eventSubscriber?: Subscription;
  currentSearch: string;

  constructor(
    protected overheardCommentService: OverheardCommentService,
    protected eventManager: JhiEventManager,
    protected modalService: NgbModal,
    protected activatedRoute: ActivatedRoute
  ) {
    this.currentSearch =
      this.activatedRoute.snapshot && this.activatedRoute.snapshot.queryParams['search']
        ? this.activatedRoute.snapshot.queryParams['search']
        : '';
  }

  loadAll(): void {
    if (this.currentSearch) {
      this.overheardCommentService
        .search({
          query: this.currentSearch,
        })
        .subscribe((res: HttpResponse<IOverheardComment[]>) => (this.overheardComments = res.body || []));
      return;
    }

    this.overheardCommentService.query().subscribe((res: HttpResponse<IOverheardComment[]>) => (this.overheardComments = res.body || []));
  }

  search(query: string): void {
    this.currentSearch = query;
    this.loadAll();
  }

  ngOnInit(): void {
    this.loadAll();
    this.registerChangeInOverheardComments();
  }

  ngOnDestroy(): void {
    if (this.eventSubscriber) {
      this.eventManager.destroy(this.eventSubscriber);
    }
  }

  trackId(index: number, item: IOverheardComment): number {
    // eslint-disable-next-line @typescript-eslint/no-unnecessary-type-assertion
    return item.id!;
  }

  registerChangeInOverheardComments(): void {
    this.eventSubscriber = this.eventManager.subscribe('overheardCommentListModification', () => this.loadAll());
  }

  delete(overheardComment: IOverheardComment): void {
    const modalRef = this.modalService.open(OverheardCommentDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.overheardComment = overheardComment;
  }
}
