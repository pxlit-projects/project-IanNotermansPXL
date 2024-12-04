import {inject, Injectable} from '@angular/core';
import {environment} from "../../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private baseUrl = environment.reviewApiUrl + "/api/review";
  http: HttpClient = inject(HttpClient);

  reviewPost(postId: number, editor: string, approved: boolean, reviewComment: string): Observable<any> {
    const reviewData = { editor, approved, reviewComment };
    return this.http.post(`${this.baseUrl}/${postId}`, reviewData);
  }
}
