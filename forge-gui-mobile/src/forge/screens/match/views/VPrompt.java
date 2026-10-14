package forge.screens.match.views;

import forge.Forge;
import org.apache.commons.lang3.StringUtils;

import com.badlogic.gdx.utils.Align;

import forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinColor.Colors;
import forge.assets.FSkinFont;
import forge.assets.TextRenderer;
import forge.card.CardZoom;
import forge.game.GameView;
import forge.game.card.CardView;
import forge.game.player.PlayerView;
import forge.menu.FMagnifyView;
import forge.toolbox.FButton;
import forge.toolbox.FButton.Corner;
import forge.toolbox.FContainer;
import forge.toolbox.FDisplayObject;
import forge.toolbox.FEvent.FEventHandler;
import forge.util.TextBounds;
import forge.util.Utils;

public class VPrompt extends FContainer {
    public static final float HEIGHT = Utils.AVG_FINGER_HEIGHT;
    public static final float BTN_WIDTH = HEIGHT * 1.5f;
    public static final float PADDING = Utils.scale(2);
    public static final FSkinFont FONT = FSkinFont.get(14);
    private static final float STATUS_HEIGHT = Utils.scale(26);
    private static final float ACTION_PADDING = Utils.scale(8);
    private static final FSkinFont ACTION_FONT = FSkinFont.get(16);
    public static FSkinColor getBackColor() {
        if (Forge.isMobileAdventureMode)
            return FSkinColor.get(Colors.ADV_CLR_THEME2);
        return FSkinColor.get(Colors.CLR_THEME2);
    }
    public static FSkinColor getForeColor() {
        if (Forge.isMobileAdventureMode)
            return FSkinColor.get(Colors.ADV_CLR_TEXT);
        return FSkinColor.get(Colors.CLR_TEXT);
    }

    private final FButton btnOk, btnCancel;
    private final FButton btnPreview;
    private final StatusLabel lblStatus;
    private final MessageLabel lblMessage;
    private String message;
    private CardView card;
    private String turnStatus = "";
    private String priorityStatus = "";

    private boolean useAdventureLayout() {
        return Forge.isMobileAdventureMode && Forge.isLandscapeMode();
    }

    public float getPreferredHeight() {
        return useAdventureLayout() ? HEIGHT + STATUS_HEIGHT + ACTION_PADDING : HEIGHT;
    }

    public void updateDuelContext(final GameView game) {
        if (game == null) {
            turnStatus = "";
            priorityStatus = "";
            return;
        }
        final StringBuilder turn = new StringBuilder(Forge.getLocalizer().getMessage("lblTurn"));
        turn.append(' ').append(game.getTurn());
        if (game.getPlayerTurn() != null) {
            turn.append("  ·  ").append(game.getPlayerTurn().getName());
        }
        if (game.getPhase() != null) {
            turn.append("  ·  ").append(game.getPhase().nameForUi);
        }
        turnStatus = turn.toString();
        priorityStatus = Forge.getLocalizer().getMessage("lblPriority") + ": —";
        if (game.getPlayers() != null) {
            for (final PlayerView player : game.getPlayers()) {
                if (player.getHasPriority()) {
                    priorityStatus = Forge.getLocalizer().getMessage("lblPriority") + ": " + player.getName();
                    break;
                }
            }
        }
    }

    public void setCardView(final CardView card) {
        this.card = card;
        btnPreview.setEnabled(card != null);
    }

    public VPrompt(String okText, String cancelText, FEventHandler okCommand, FEventHandler cancelCommand) {
        lblMessage = add(new MessageLabel());
        lblMessage.setLeft(BTN_WIDTH);
        lblMessage.setHeight(HEIGHT);
        btnOk = add(new FButton(okText, okCommand));
        btnCancel = add(new FButton(cancelText, cancelCommand));
        lblStatus = add(new StatusLabel());
        btnPreview = add(new FButton(Forge.getLocalizer().getMessage("lblCardPreview"), e -> {
            if (card != null) {
                CardZoom.show(card);
            }
        }));
        btnPreview.setEnabled(false);
        btnPreview.setVisible(false);
        lblStatus.setVisible(false);
        btnOk.setSize(BTN_WIDTH, HEIGHT);
        btnCancel.setSize(BTN_WIDTH, HEIGHT);
        btnOk.setCorner(Corner.BottomLeft);
        btnCancel.setCorner(Corner.BottomRight);
        btnOk.setEnabled(false); //disable buttons until first input queued
        btnCancel.setEnabled(false);
    }

    public FButton getBtnOk() {
        return btnOk;
    }

    public FButton getBtnCancel() {
        return btnCancel;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message0) {
        message = message0;
        setCardView(null);
    }
    public void setMessage(String message0, CardView card0) {
        message = message0;
        setCardView(card0);
    }

    /** Flashes animation on input panel if play is currently waiting on input. */
    public void remind() {
        //SDisplayUtil.remind(view);
    }

    @Override
    protected void doLayout(float width, float height) {
        final boolean adventure = useAdventureLayout();
        lblStatus.setVisible(adventure);
        btnPreview.setVisible(adventure);
        if (adventure) {
            final float gap = ACTION_PADDING;
            final float actionY = STATUS_HEIGHT + gap / 2;
            final float actionHeight = height - actionY - gap / 2;
            final float buttonWidth = Math.min(width * 0.18f, HEIGHT * 2.8f);
            lblStatus.setBounds(gap, 0, Math.max(0, width - 2 * gap), STATUS_HEIGHT);
            final FButton first = Forge.reversedPrompt ? btnCancel : btnOk;
            final FButton second = Forge.reversedPrompt ? btnOk : btnCancel;
            second.setBounds(width - gap - buttonWidth, actionY, buttonWidth, actionHeight);
            first.setBounds(second.getLeft() - gap - buttonWidth, actionY, buttonWidth, actionHeight);
            btnPreview.setBounds(first.getLeft() - gap - buttonWidth, actionY, buttonWidth, actionHeight);
            btnOk.setCorner(Corner.None);
            btnCancel.setCorner(Corner.None);
            btnOk.setFont(ACTION_FONT);
            btnCancel.setFont(ACTION_FONT);
            btnPreview.setFont(FONT);
            lblMessage.setBounds(gap, actionY, Math.max(0, btnPreview.getLeft() - 2 * gap), actionHeight);
            return;
        }
        btnOk.setFont(FONT);
        btnCancel.setFont(FONT);
        btnOk.setBounds(0, 0, BTN_WIDTH, height);
        btnCancel.setBounds(0, 0, BTN_WIDTH, height);
        lblMessage.setBounds(BTN_WIDTH, 0, Math.max(0, width - 2 * BTN_WIDTH), height);
        lblMessage.setWidth(width - 2 * BTN_WIDTH);
        if (Forge.reversedPrompt) {
            btnOk.setCorner(Corner.BottomRight);
            btnCancel.setCorner(Corner.BottomLeft);
            btnOk.setLeft(lblMessage.getRight());
        } else {
            btnOk.setCorner(Corner.BottomLeft);
            btnCancel.setCorner(Corner.BottomRight);
            btnCancel.setLeft(lblMessage.getRight());
        }
    }

    @Override
    protected void drawBackground(Graphics g) {
        g.fillRect(getBackColor(), 0, 0, getWidth(), getHeight());
        if (useAdventureLayout()) {
            g.fillRect(getForeColor().alphaColor(0.07f), 0, 0, getWidth(), STATUS_HEIGHT);
            g.drawLine(1, getForeColor().alphaColor(0.18f), 0, STATUS_HEIGHT, getWidth(), STATUS_HEIGHT);
        }
    }

    private class StatusLabel extends FDisplayObject {
        @Override
        public void draw(Graphics g) {
            final float priorityWidth = getWidth() * 0.34f;
            g.drawText(turnStatus, FONT, getForeColor(), 0, 0,
                    getWidth() - priorityWidth - ACTION_PADDING, getHeight(), false, Align.left, true);
            g.drawText(priorityStatus, FONT, getForeColor(), getWidth() - priorityWidth, 0,
                    priorityWidth, getHeight(), false, Align.right, true);
        }
    }
    
    private class MessageLabel extends FDisplayObject {
        private final TextRenderer renderer = new TextRenderer();

        @Override
        public boolean tap(float x, float y, int count) {
            //if not enough room for prompt at given size, show magnify view
            float maxWidth = getWidth() - 2 * PADDING;
            float maxHeight = getHeight() - 2 * PADDING;
            final FSkinFont font = useAdventureLayout() ? ACTION_FONT : FONT;
            TextBounds textBounds = renderer.getWrappedBounds(message, font, maxWidth);
            if (textBounds.height > maxHeight) {
                FMagnifyView.show(this, message, getForeColor(), getBackColor(), font, false);
            }
            return true;
        }

        @Override
        public boolean fling(float x, float y) {
            if (card != null) {
                CardZoom.show(card);
            }
            return true;
        }

        @Override
        public boolean longPress(float x, float y) {
            if (card != null) {
                CardZoom.show(card);
            }
            return true;
        }

        @Override
        public void draw(Graphics g) {
            if (!StringUtils.isEmpty(message)) {
                float x = PADDING;
                float y = PADDING;
                float w = getWidth() - 2 * PADDING;
                float h = getHeight() - 2 * PADDING;
                renderer.drawText(g, message, useAdventureLayout() ? ACTION_FONT : FONT,
                        getForeColor(), x, y, w, h, y, h, true,
                        useAdventureLayout() ? Align.left : Align.center, true);
            }
        }
    }
}
