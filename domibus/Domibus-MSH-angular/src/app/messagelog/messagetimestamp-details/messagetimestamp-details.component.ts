import {Component, OnInit, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {DownloadService} from "../../common/download.service";

@Component({
  selector: 'app-messagetimestamp-details',
  templateUrl: './messagetimestamp-details.component.html',
  styleUrls: ['./messagetimestamp-details.component.css']
})
export class MessageTimestampDetailsComponent {

  timestampInfo;

  constructor(public dialogRef: MatDialogRef<MessageTimestampDetailsComponent>, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.timestampInfo = data.timestampInfo;
  }

  async openURL(url) {
    window.open(url, '_blank');
  }
}
