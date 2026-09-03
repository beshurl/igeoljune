import { defineStore } from "pinia";
import {
  fetchRecipients,
  createRecipient,
  updateRecipient,
  deleteRecipient,
} from "../api/recipients";

export const useRecipientStore = defineStore("recipient", {
  state: () => ({
    recipients: [],
    selectedRecipientId: null,
  }),
  getters: {
    selectedRecipient: (state) =>
      state.recipients.find((r) => r.id === state.selectedRecipientId) || null,
  },
  actions: {
    async loadRecipients() {
      this.recipients = await fetchRecipients();
    },
    async addRecipient(payload) {
      const created = await createRecipient(payload);
      this.recipients.push(created);
      return created;
    },
    async editRecipient(recipientId, payload) {
      const updated = await updateRecipient(recipientId, payload);
      const idx = this.recipients.findIndex((r) => r.id === recipientId);
      if (idx !== -1) this.recipients[idx] = updated;
      return updated;
    },
    async removeRecipient(recipientId) {
      await deleteRecipient(recipientId);
      this.recipients = this.recipients.filter((r) => r.id !== recipientId);
    },
    select(recipientId) {
      this.selectedRecipientId = recipientId;
    },
  },
});
