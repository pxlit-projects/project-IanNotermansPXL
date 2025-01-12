import { CanDeactivateFn } from '@angular/router';
import {AddPostComponent} from "../../core/posts/add-post/add-post.component";

export const routeGuard: CanDeactivateFn<AddPostComponent> = (component, currentRoute, currentState, nextState) => {
  if (component.postForm.dirty && component.isFormSubmitted === false) {
    return window.confirm('Do you really want to leave?');
  } else {
    return true;
  }
};
