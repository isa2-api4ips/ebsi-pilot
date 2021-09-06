export class MessageTimestampEntry {
  constructor(public messageId: string,
              public timestamp: Date,
              public notarizedHashValue: string,
              public signatureHashValue: string,
              public transactionHash: string,
              public registeredBy: string,
              public validationUrl: string,
              public isValidSignatureHash: string
  ) {

  }
}
