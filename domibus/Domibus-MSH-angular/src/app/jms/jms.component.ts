import {AfterViewChecked, AfterViewInit, ChangeDetectorRef, Component, EventEmitter, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AlertService} from '../common/alert/alert.service';
import {MessagesRequestRO} from './ro/messages-request-ro';
import {MatDialog, MatDialogRef} from '@angular/material';
import {MoveDialogComponent} from './move-dialog/move-dialog.component';
import {MessageDialogComponent} from './message-dialog/message-dialog.component';
import {DirtyOperations} from '../common/dirty-operations';
import {Observable} from 'rxjs/Observable';
import mix from '../common/mixins/mixin.utils';
import BaseListComponent from '../common/mixins/base-list.component';
import FilterableListMixin from '../common/mixins/filterable-list.mixin';
import {DialogsService} from '../common/dialogs/dialogs.service';
import ModifiableListMixin from '../common/mixins/modifiable-list.mixin';
import {ClientPageableListMixin} from '../common/mixins/pageable-list.mixin';
import {ClientSortableListMixin} from '../common/mixins/sortable-list.mixin';
import {ApplicationContextService} from '../common/application-context.service';
import {ComponentName} from '../common/component-name-decorator';

@Component({
  selector: 'app-jms',
  templateUrl: './jms.component.html',
  styleUrls: ['./jms.component.css']
})
@ComponentName('JMS Messages')
export class JmsComponent extends mix(BaseListComponent)
  .with(FilterableListMixin, ClientPageableListMixin, ModifiableListMixin, ClientSortableListMixin)
  implements OnInit, DirtyOperations, AfterViewInit, AfterViewChecked {

  timestampFromMaxDate: Date;
  timestampToMinDate: Date;
  timestampToMaxDate: Date;

  defaultQueueSet: EventEmitter<boolean>;
  queuesInfoGot: EventEmitter<boolean>;

  @ViewChild('rowWithDateFormatTpl', {static: false}) rowWithDateFormatTpl: TemplateRef<Object>;
  @ViewChild('rowActions', {static: false}) rowActions: TemplateRef<any>;
  @ViewChild('rawTextTpl', {static: false}) public rawTextTpl: TemplateRef<any>;

  queues: any[];
  orderedQueues: any[];

  currentSearchSelectedSource;

  markedForDeletionMessages: any[];

  request: MessagesRequestRO;

  private _selectedSource: any;

  get selectedSource(): any {
    return this._selectedSource;
  }

  set selectedSource(value: any) {
    const oldVal = this._selectedSource;
    this._selectedSource = value;
    this.filter.source = value.name;
    this.defaultQueueSet.emit(oldVal);
  }

  constructor(private applicationService: ApplicationContextService, private http: HttpClient, private alertService: AlertService,
              public dialog: MatDialog, private dialogsService: DialogsService, private changeDetector: ChangeDetectorRef) {
    super();
  }

  ngOnInit() {
    super.ngOnInit();

    super.filter = new MessagesRequestRO();

    this.timestampFromMaxDate = new Date();
    this.timestampToMinDate = null;
    this.timestampToMaxDate = new Date();

    this.defaultQueueSet = new EventEmitter(false);
    this.queuesInfoGot = new EventEmitter(false);

    this.queues = [];
    this.orderedQueues = [];

    // set toDate equals to now
    this.filter.toDate = new Date();
    this.filter.toDate.setHours(23, 59, 59, 999);

    this.markedForDeletionMessages = [];

    this.loadDestinations();

    this.queuesInfoGot.subscribe(result => {
      this.setDefaultQueue('.*?[d|D]omibus.?DLQ');
    });

    this.defaultQueueSet.subscribe(oldVal => {
      super.tryFilter(false).then(done => {
        if (!done) {
          //revert the drop-down value to the old one
          this._selectedSource = oldVal;
        }
      });
    });
  }

  ngAfterViewInit() {
    this.columnPicker.allColumns = [
      {
        name: 'ID',
        prop: 'id'
      },
      {
        name: 'JMS Type',
        prop: 'type',
        width: 80
      },
      {
        cellTemplate: this.rowWithDateFormatTpl,
        name: 'Time',
        prop: 'timestamp',
        width: 80
      },
      {
        name: 'Custom prop',
        prop: 'customPropertiesText',
        width: 250
      },
      {
        name: 'JMS prop',
        prop: 'jmspropertiesText',
        width: 200
      },
      {
        cellTemplate: this.rowActions,
        name: 'Actions',
        width: 10,
        sortable: false
      }

    ];

    this.columnPicker.selectedColumns = this.columnPicker.allColumns.filter(col => {
      return ['ID', 'Time', 'Custom prop', 'JMS prop', 'Actions'].indexOf(col.name) != -1
    });
  }

  ngAfterViewChecked() {
    this.changeDetector.detectChanges();
  }

  private getDestinations(): Observable<any> {
    return this.http.get<any>('rest/jms/destinations')
      .map(response => response.jmsDestinations)
      .catch((error) => this.alertService.exception('Could not load queues ', error));
  }

  private loadDestinations() {
    this.getDestinations().subscribe(
      (destinations) => {
        this.queues = [];
        for (const key in destinations) {
          this.queues.push(destinations[key]);
        }
        this.queuesInfoGot.emit();
      }
    );
  }

  private refreshDestinations(): Observable<any> {
    const result = this.getDestinations();
    result.subscribe(
      (destinations) => {
        for (const key in destinations) {
          var src = destinations[key];
          const queue = this.queues.find(el => el.name === src.name);
          if (queue) {
            Object.assign(queue, src);
          }
        }
      }
    );
    return result;
  }

  private setDefaultQueue(queueName: string) {
    if (!this.queues || this.queues.length == 0) {
      return;
    }

    const matching = this.queues.find((el => el.name && el.name.match(queueName)));
    const toSelect = matching != null ? matching : this.queues.length[0];

    this.selectedSource = toSelect;
  }

  edit(row) {
    this.showDetails(row);
  }

  onTimestampFromChange(event) {
    this.timestampToMinDate = event.value;
  }

  onTimestampToChange(event) {
    this.timestampFromMaxDate = event.value;
  }

  canSearch() {
    return this.filter.source && super.canSearch();
  }

  protected get GETUrl(): string {
    return 'rest/jms/messages';
  }

  protected async onBeforeGetData(): Promise<any> {
    if (!this.filter.source) {
      return Promise.reject('Source should be set');
    }

    this.markedForDeletionMessages = [];
    this.currentSearchSelectedSource = this.selectedSource;
  }

  protected onLoadDataError(error) {
    this.alertService.error('An error occurred while loading the JMS messages. In case you are using the Selector / JMS Type, please follow the rules for Selector / JMS Type according to Help Page / Admin Guide');
    console.log('Error: ', error.status, error.error);
    error.handled = true;
  }

  public setServerResults(res) {
    const rows: any[] = res.messages;
    rows.forEach(row => {
      row.customPropertiesText = JSON.stringify(row.customProperties);
      row.jmspropertiesText = JSON.stringify(row.jmsproperties);
    });
    super.rows = rows;
    super.count = rows.length;
    this.refreshDestinations();
  }

  async doSave(): Promise<any> {
    const messageIds = this.markedForDeletionMessages.map((message) => message.id);
    // because the user can change the source after pressing search and then select the messages and press delete
    // in this case I need to use currentSearchSelectedSource
    return this.serverRemove(this.currentSearchSelectedSource.name, messageIds);
  }

  moveElements(elements: any[]) {
    if (!elements || elements.length == 0) {
      return;
    }

    try {
      let queues = this.getAllowedDestinationQueues(elements);
      this.dialog.open(MoveDialogComponent, {data: {queues: queues}})
        .afterClosed().subscribe(result => {
        if (result && result.destination) {
          const messageIds = elements.map((message) => message.id);
          this.serverMove(this.currentSearchSelectedSource.name, result.destination, messageIds);
        }
      });
    } catch (ex) {
      this.alertService.exception('Exception trying to move messages:', ex);
    }
  }

  private getAllowedDestinationQueues(messages: any[]) {
    let originalQueueName: any;
    if (messages.length > 1) {
      originalQueueName = this.getCommonOriginalQueueName(messages);
    } else {
      const message = messages[0];
      originalQueueName = this.getOriginalQueueName(message);
    }
    console.log(`Original queue name for the message is [${this.selectedSource.name}].`);

    let allowedQueues: any[];
    if (originalQueueName) {
      allowedQueues = this.queues.filter(queue => queue.name.includes(originalQueueName));
      if (allowedQueues.length == 0) {
        throw new Error(`Cannot move the selected messages because the original queue [${originalQueueName}] cannot be found.`);
      }
    } else {
      console.warn(`Could not find the original queue [${originalQueueName}] for the selected message; returning all as allowed destination queues.`);
      allowedQueues = this.queues;
    }

    // exclude source queue
    console.log(`Excluding the current queue [${this.selectedSource.name}] from the allowed destination queues.`);
    allowedQueues = allowedQueues.filter(el => el.name != this.selectedSource.name);
    if (allowedQueues.length == 0) {
      throw new Error(`Cannot move the selected messages because the original queue [${originalQueueName}] is the same as the current queue.`);
    }
    return allowedQueues;
  }

  getCommonOriginalQueueName(messages: any[]): any {
    let originaleQueueNames = messages.map(msg => this.getOriginalQueueName(msg))
      .filter((msg, index, list) => list.indexOf(msg) === index);

    if (originaleQueueNames.length > 1) {
      throw new Error('Cannot move the messages because they have different original/destination queues.');
    }
    if (originaleQueueNames.length == 1) {
      return originaleQueueNames[0];
    }
    return null;
  }

  getOriginalQueueName(message): any {
    let originalQueueName = message.customProperties.originalQueue;
    if (!originalQueueName) {
      return null;
    }
    // EDELIVERY-2814
    originalQueueName = originalQueueName.substr(originalQueueName.indexOf('!') + 1);
    return originalQueueName;
  }

  moveSelected() {
    this.moveElements(this.selected);
  }

  moveAction(row) {
    this.moveElements([row]);
  }

  showDetails(selectedRow: any) {
    const dialogRef: MatDialogRef<MessageDialogComponent> = this.dialog.open(MessageDialogComponent);
    dialogRef.componentInstance.message = selectedRow;
    dialogRef.componentInstance.currentSearchSelectedSource = this.currentSearchSelectedSource;
  }

  deleteAction(row) {
    this.deleteElements([row]);
  }

  delete() {
    this.deleteElements(this.selected);
  }

  deleteElements(elements: any[]) {
    elements.forEach(el => {
      const index = this.rows.indexOf(el);
      if (index > -1) {
        this.rows.splice(index, 1);
        this.markedForDeletionMessages.push(el);
      }
    });
    super.rows = [...this.rows];
    super.count = this.rows.length;

    super.selected = [];
  }

  serverMove(source: string, destination: string, messageIds: Array<any>) {
    super.isSaving = true;
    this.http.post('rest/jms/messages/action', {
      source: source,
      destination: destination,
      selectedMessages: messageIds,
      action: 'MOVE'
    }).subscribe(
      () => {
        this.alertService.success('The operation \'move messages\' completed successfully.');

        // refresh destinations
        this.refreshDestinations().subscribe(res => {
          this.setDefaultQueue(this.currentSearchSelectedSource.name);
        });

        // remove the selected rows
        this.deleteElements(this.selected);
        this.markedForDeletionMessages = [];

        super.isSaving = false;
      },
      error => {
        this.alertService.exception('The operation \'move messages\' could not be completed: ', error);
        super.isSaving = false;
      }
    )
  }

  serverRemove(source: string, messageIds: Array<any>): Promise<any> {
    return this.http.post('rest/jms/messages/action', {
      source: source,
      selectedMessages: messageIds,
      action: 'REMOVE'
    }).toPromise().then(() => {
        this.refreshDestinations();
        this.markedForDeletionMessages = [];
      }
    )
  }

  saveAsCSV() {
    if (!this.activeFilter.source) {
      this.alertService.error('Source should be set');
      return;
    }

    super.saveAsCSV();
  }

  get csvUrl(): string {
    return 'rest/jms/csv?' + this.createAndSetParameters();
  }

  isDirty(): boolean {
    return this.markedForDeletionMessages && this.markedForDeletionMessages.length > 0;
  }

  canCancel() {
    return this.isDirty() && !this.isBusy();
  }

  canSave() {
    return this.canCancel();
  }

  canDelete() {
    return this.atLeastOneRowSelected() && !this.isBusy();
  }

  canMove() {
    return this.canDelete();
  }

  private atLeastOneRowSelected() {
    return this.selected.length > 0;
  }


}
